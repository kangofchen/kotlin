/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa.new

import org.jetbrains.kotlin.fir.contracts.description.ConeBooleanConstantReference
import org.jetbrains.kotlin.fir.contracts.description.ConeConditionalEffectDeclaration
import org.jetbrains.kotlin.fir.contracts.description.ConeConstantReference
import org.jetbrains.kotlin.fir.contracts.description.ConeReturnsEffectDeclaration
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.ImplicitReceiverStackImpl
import org.jetbrains.kotlin.fir.resolve.ResolutionMode
import org.jetbrains.kotlin.fir.resolve.dfa.*
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.*
import org.jetbrains.kotlin.fir.resolve.dfa.contracts.buildContractFir
import org.jetbrains.kotlin.fir.resolve.dfa.contracts.createArgumentsMapping
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirAbstractBodyResolveTransformer
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.UniversalConeInferenceContext
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.resultType
import org.jetbrains.kotlin.fir.resolve.withNullability
import org.jetbrains.kotlin.fir.symbols.AbstractFirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.CallableId
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.AbstractTypeChecker
import kotlin.IllegalArgumentException

class FirDataFlowAnalyzer(private val components: FirAbstractBodyResolveTransformer.BodyResolveTransformerComponents) {
    companion object {
        internal val KOTLIN_BOOLEAN_NOT = CallableId(FqName("kotlin"), FqName("Boolean"), Name.identifier("not"))
    }

    private val context: UniversalConeInferenceContext = components.inferenceComponents.ctx
    private val receiverStack: ImplicitReceiverStackImpl = components.implicitReceiverStack as ImplicitReceiverStackImpl

    private val graphBuilder = ControlFlowGraphBuilder()
    private val logicSystem: LogicSystem = TODO()
    private val variableStorage = VariableStorage()
    private val flowOnNodes = mutableMapOf<CFGNode<*>, Flow>()

    private val variablesForWhenConditions = mutableMapOf<WhenBranchConditionExitNode, DataFlowVariable>()

    private var contractDescriptionVisitingMode = false

    private val any = components.session.builtinTypes.anyType.coneTypeUnsafe<ConeKotlinType>()
    private val nullableNothing = components.session.builtinTypes.nullableNothingType.coneTypeUnsafe<ConeKotlinType>()

    // ----------------------------------- Requests -----------------------------------

    fun getTypeUsingSmartcastInfo(qualifiedAccessExpression: FirQualifiedAccessExpression): Collection<ConeKotlinType>? {
        /*
         * DataFlowAnalyzer holds variables only for declarations that have some smartcast (or can have)
         * If there is no useful information there is no data flow variable also
         */
        val symbol: AbstractFirBasedSymbol<*> = qualifiedAccessExpression.symbol ?: return null
        val variable = variableStorage[symbol] ?: return null
        val typesFromInfo = graphBuilder.lastNode.flow.getKnownInfo(variable)?.exactType

        /*
         *If we have local variable that points to some other declaration then
         *   we should add type of that declaration as possible type of variable
         *
         *   val s: String = ""
         *   val x: Any
         *   x = ""
         *   x.length()  <- x smartcasted to String
         */
        if (variable.symbol != symbol) {
            val originalType = qualifiedAccessExpression.coneType
            val type = when (val fir = variable.symbol.fir) {
                is FirTypedDeclaration -> fir.returnTypeRef.coneTypeUnsafe<ConeKotlinType>()
                is FirExpression -> fir.coneType
                else -> return typesFromInfo
            }

            if (!AbstractTypeChecker.isSubtypeOf(context, type, originalType)) {
                return mutableListOf(type).apply {
                    this += typesFromInfo ?: emptyList()
                }
            }
        }
        return typesFromInfo
    }

    fun returnExpressionsOfAnonymousFunction(function: FirAnonymousFunction): List<FirStatement> {
        return graphBuilder.returnExpressionsOfAnonymousFunction(function)
    }

    // ----------------------------------- Named function -----------------------------------

    fun enterFunction(function: FirFunction<*>) {
        val (functionEnterNode, previousNode) = graphBuilder.enterFunction(function)
        if (previousNode == null) {
            functionEnterNode.mergeIncomingFlow()
        } else {
            // Enter anonymous function
            assert(functionEnterNode.previousNodes.isEmpty())
            functionEnterNode.flow = logicSystem.forkFlow(previousNode.flow)
        }
    }

    fun exitFunction(function: FirFunction<*>): ControlFlowGraph? {
        val (node, graph) = graphBuilder.exitFunction(function)
        if (function.body == null) {
            node.mergeIncomingFlow()
        }
        if (!graphBuilder.isTopLevel()) {
            for (valueParameter in function.valueParameters) {
                variableStorage.removeRealVariable(valueParameter.symbol)
            }
        }
        if (graphBuilder.isTopLevel()) {
            flowOnNodes.clear()
            variableStorage.reset()
            graphBuilder.reset()
        }
        return graph
    }

    // ----------------------------------- Property -----------------------------------

    fun enterProperty(property: FirProperty) {
        graphBuilder.enterProperty(property).mergeIncomingFlow()
    }

    fun exitProperty(property: FirProperty): ControlFlowGraph {
        val (node, graph) = graphBuilder.exitProperty(property)
        node.mergeIncomingFlow()
        return graph
    }

    // ----------------------------------- Block -----------------------------------

    fun enterBlock(block: FirBlock) {
        graphBuilder.enterBlock(block)?.mergeIncomingFlow()
    }

    fun exitBlock(block: FirBlock) {
        graphBuilder.exitBlock(block).mergeIncomingFlow()
    }

    // ----------------------------------- Operator call -----------------------------------

    fun exitTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall) {
        val node = graphBuilder.exitTypeOperatorCall(typeOperatorCall).mergeIncomingFlow()
        if (typeOperatorCall.operation !in FirOperation.TYPES) return
        val type = typeOperatorCall.conversionTypeRef.coneTypeUnsafe<ConeKotlinType>()
        val operandVariable = variableStorage.getOrCreateRealVariable(typeOperatorCall.argument.symbol) ?: return
        val flow = node.flow

        when (typeOperatorCall.operation) {
            FirOperation.IS, FirOperation.NOT_IS -> {
                val expressionVariable = variableStorage.createSyntheticVariable(typeOperatorCall.argument)

                val hasTypeInfo = operandVariable has type
                val hasNotTypeInfo = operandVariable hasNot type

                fun chooseInfo(trueBranch: Boolean) =
                    if ((typeOperatorCall.operation == FirOperation.IS) == trueBranch) hasTypeInfo else hasNotTypeInfo

                flow.addLogicStatement((expressionVariable eq true) implies chooseInfo(true))
                flow.addLogicStatement((expressionVariable eq false) implies chooseInfo(false))
            }

            FirOperation.AS -> {
                flow.addKnownInfo(operandVariable has type)
            }

            FirOperation.SAFE_AS -> {
                val expressionVariable = variableStorage.createSyntheticVariable(typeOperatorCall.argument)
                flow.addLogicStatement((expressionVariable notEq null) implies (operandVariable has type))
                flow.addLogicStatement((expressionVariable eq null) implies (operandVariable hasNot type))
            }

            else -> throw IllegalStateException()
        }
        node.flow = flow
    }

    fun exitOperatorCall(operatorCall: FirOperatorCall) {
        val node = graphBuilder.exitOperatorCall(operatorCall).mergeIncomingFlow()
        when (val operation = operatorCall.operation) {
            FirOperation.EQ, FirOperation.NOT_EQ, FirOperation.IDENTITY, FirOperation.NOT_IDENTITY -> {
                val leftOperand = operatorCall.arguments[0]
                val rightOperand = operatorCall.arguments[1]

                val leftConst = leftOperand as? FirConstExpression<*>
                val rightConst = rightOperand as? FirConstExpression<*>

                when {
                    leftConst != null && rightConst != null -> return
                    leftConst?.kind == FirConstKind.Null -> processEqNull(node, rightOperand, operation)
                    rightConst?.kind == FirConstKind.Null -> processEqNull(node, leftOperand, operation)
                    leftConst != null -> processEqWithConst(node, rightOperand, leftConst, operation)
                    rightConst != null -> processEqWithConst(node, leftOperand, rightConst, operation)
                    else -> processEq(node, leftOperand, rightOperand, operation)
                }
            }
        }
    }

    // const != null
    private fun processEqWithConst(node: OperatorCallNode, operand: FirExpression, const: FirConstExpression<*>, operation: FirOperation) {
        val isEq = operation.isEq()
        val expressionVariable = variableStorage.createSyntheticVariable(node.fir)
        val flow = node.flow
        val operandVariable = variableStorage.getOrCreateVariable(operand)
        // expression == const -> expression != null
        flow.addLogicStatement((expressionVariable eq isEq) implies (operandVariable has any))

        // propagating facts for (... == true) and (... == false)
        if (const.kind == FirConstKind.Boolean) {
            val constValue = const.value as Boolean
            val shouldInvert = isEq xor constValue

            logicSystem.translateConditionalVariableInStatements(
                flow,
                operandVariable,
                expressionVariable,
                shouldRemoveOriginalStatements = operandVariable.isSynthetic()
            ) {
                if (shouldInvert) (it.condition) implies (it.effect.invert())
                else it
            }
        }
    }

    private fun processEq(node: OperatorCallNode, leftOperand: FirExpression, rightOperand: FirExpression, operation: FirOperation) {
        val leftIsNullable = leftOperand.coneType.isMarkedNullable
        val rightIsNullable = rightOperand.coneType.isMarkedNullable
        // left == right && right not null -> left != null
        when {
            leftIsNullable && rightIsNullable -> return
            leftIsNullable -> processEqNull(node, leftOperand, operation.invert())
            rightIsNullable -> processEqNull(node, rightOperand, operation.invert())
        }
    }

    private fun processEqNull(node: OperatorCallNode, operand: FirExpression, operation: FirOperation) {
        val flow = node.flow
        val expressionVariable = variableStorage.createSyntheticVariable(node.fir)
        val operandVariable = variableStorage.getOrCreateVariable(operand)

        val isEq = operation.isEq()

        val predicate = when (isEq) {
            true -> operandVariable eq null
            false -> operandVariable notEq null
        }

        logicSystem.approvePredicate(flow, predicate).forEach { effect ->
            flow.addLogicStatement((expressionVariable eq true) implies effect)
            flow.addLogicStatement((expressionVariable eq false) implies effect.invert())
        }

        if (operandVariable is RealVariable) {
            flow.addLogicStatement((expressionVariable eq isEq implies (operandVariable has any)))
            flow.addLogicStatement((expressionVariable notEq isEq) implies (operandVariable hasNot any))

//            TODO: design do we need casts to Nothing?
//            flow.addLogicStatement((expressionVariable eq !isEq) implies (operandVariable has nullableNothing))
//            flow.addLogicStatement((expressionVariable notEq !isEq) implies (operandVariable hasNot nullableNothing))
        }
    }

    // ----------------------------------- Jump -----------------------------------

    fun exitJump(jump: FirJump<*>) {
        graphBuilder.exitJump(jump).mergeIncomingFlow()
    }

    // ----------------------------------- Check not null call -----------------------------------

    fun exitCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall) {
        // Add `Any` to the set of possible types; the intersection type `T? & Any` will be reduced to `T` after smartcast.
        val node = graphBuilder.exitCheckNotNullCall(checkNotNullCall).mergeIncomingFlow()
        val operandVariable = variableStorage.getOrCreateRealVariable(checkNotNullCall.argument.symbol) ?: return
        node.flow.addKnownInfo(operandVariable has any)
    }

    // ----------------------------------- When -----------------------------------

    fun enterWhenExpression(whenExpression: FirWhenExpression) {
        graphBuilder.enterWhenExpression(whenExpression).mergeIncomingFlow()
    }

    fun enterWhenBranchCondition(whenBranch: FirWhenBranch) {
        val node = graphBuilder.enterWhenBranchCondition(whenBranch).mergeIncomingFlow()
        val previousNode = node.previousNodes.single()
        if (previousNode is WhenBranchConditionExitNode) {
            val conditionVariable = variablesForWhenConditions.remove(previousNode)!!
            node.flow = logicSystem.approveStatementsInsideFlow(
                node.flow,
                conditionVariable eq false,
                shouldForkFlow = true,
                shouldRemoveSynthetics = true
            )
        }
    }

    fun exitWhenBranchCondition(whenBranch: FirWhenBranch) {
        val (conditionExitNode, branchEnterNode) = graphBuilder.exitWhenBranchCondition(whenBranch)
        conditionExitNode.mergeIncomingFlow()

        val conditionVariable = variableStorage.getOrCreateVariable(whenBranch.condition)
        variablesForWhenConditions[conditionExitNode] = conditionVariable
        branchEnterNode.flow = logicSystem.approveStatementsInsideFlow(
            conditionExitNode.flow,
            conditionVariable eq true,
            shouldForkFlow = true,
            shouldRemoveSynthetics = false
        )
    }

    fun exitWhenBranchResult(whenBranch: FirWhenBranch) {
        graphBuilder.exitWhenBranchResult(whenBranch).mergeIncomingFlow()
    }

    fun exitWhenExpression(whenExpression: FirWhenExpression) {
        val (whenExitNode, syntheticElseNode) = graphBuilder.exitWhenExpression(whenExpression)
        if (syntheticElseNode != null) {
            syntheticElseNode.mergeIncomingFlow()
            val previousConditionExitNode = syntheticElseNode.previousNodes.single() as? WhenBranchConditionExitNode
            // previous node for syntheticElseNode can be not WhenBranchConditionExitNode in case of `when` without any branches
            // in that case there will be when enter or subject access node
            if (previousConditionExitNode != null) {
                val conditionVariable = variablesForWhenConditions.remove(previousConditionExitNode)!!
                syntheticElseNode.flow = logicSystem.approveStatementsInsideFlow(
                    syntheticElseNode.flow,
                    conditionVariable eq false,
                    shouldForkFlow = true,
                    shouldRemoveSynthetics = true
                )
            }
        }
        val previousFlows = whenExitNode.alivePreviousNodes.map { it.flow }
        val flow = logicSystem.joinFlow(previousFlows)
        whenExitNode.flow = flow
        // TODO: wtf?
        // val subjectSymbol = whenExpression.subjectVariable?.symbol
        // if (subjectSymbol != null) {
        //     variableStorage[subjectSymbol]?.let { flow = flow.removeVariable(it) }
        // }
        // node.flow = flow
    }

    // ----------------------------------- While Loop -----------------------------------

    fun enterWhileLoop(loop: FirLoop) {
        val (loopEnterNode, loopConditionEnterNode) = graphBuilder.enterWhileLoop(loop)
        loopEnterNode.mergeIncomingFlow()
        loopConditionEnterNode.mergeIncomingFlow()
    }

    fun exitWhileLoopCondition(loop: FirLoop) {
        val (loopConditionExitNode, loopBlockEnterNode) = graphBuilder.exitWhileLoopCondition(loop)
        loopConditionExitNode.mergeIncomingFlow()
        loopBlockEnterNode.mergeIncomingFlow()
        variableStorage[loop.condition]?.let { conditionVariable ->
            loopBlockEnterNode.flow = logicSystem.approveStatementsInsideFlow(
                loopBlockEnterNode.flow,
                conditionVariable eq true,
                shouldForkFlow = false,
                shouldRemoveSynthetics = true
            )
        }
    }

    fun exitWhileLoop(loop: FirLoop) {
        val (blockExitNode, exitNode) = graphBuilder.exitWhileLoop(loop)
        blockExitNode.mergeIncomingFlow()
        exitNode.mergeIncomingFlow()
    }

    // ----------------------------------- Do while Loop -----------------------------------

    fun enterDoWhileLoop(loop: FirLoop) {
        val (loopEnterNode, loopBlockEnterNode) = graphBuilder.enterDoWhileLoop(loop)
        loopEnterNode.mergeIncomingFlow()
        loopBlockEnterNode.mergeIncomingFlow()
    }

    fun enterDoWhileLoopCondition(loop: FirLoop) {
        val (loopBlockExitNode, loopConditionEnterNode) = graphBuilder.enterDoWhileLoopCondition(loop)
        loopBlockExitNode.mergeIncomingFlow()
        loopConditionEnterNode.mergeIncomingFlow()
    }

    fun exitDoWhileLoop(loop: FirLoop) {
        val (loopConditionExitNode, loopExitNode) = graphBuilder.exitDoWhileLoop(loop)
        loopConditionExitNode.mergeIncomingFlow()
        loopExitNode.mergeIncomingFlow()
    }

    // ----------------------------------- Try-catch-finally -----------------------------------

    fun enterTryExpression(tryExpression: FirTryExpression) {
        val (tryExpressionEnterNode, tryMainBlockEnterNode) = graphBuilder.enterTryExpression(tryExpression)
        tryExpressionEnterNode.mergeIncomingFlow()
        tryMainBlockEnterNode.mergeIncomingFlow()
    }

    fun exitTryMainBlock(tryExpression: FirTryExpression) {
        graphBuilder.exitTryMainBlock(tryExpression).mergeIncomingFlow()
    }

    fun enterCatchClause(catch: FirCatch) {
        graphBuilder.enterCatchClause(catch).mergeIncomingFlow()
    }

    fun exitCatchClause(catch: FirCatch) {
        graphBuilder.exitCatchClause(catch).mergeIncomingFlow()
    }

    fun enterFinallyBlock(tryExpression: FirTryExpression) {
        // TODO
        graphBuilder.enterFinallyBlock(tryExpression).mergeIncomingFlow()
    }

    fun exitFinallyBlock(tryExpression: FirTryExpression) {
        // TODO
        graphBuilder.exitFinallyBlock(tryExpression).mergeIncomingFlow()
    }

    fun exitTryExpression(tryExpression: FirTryExpression) {
        // TODO
        graphBuilder.exitTryExpression(tryExpression).mergeIncomingFlow()
    }

    // ----------------------------------- Resolvable call -----------------------------------

    fun enterQualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression) {
        enterSafeCall(qualifiedAccessExpression)
    }

    private fun enterSafeCall(qualifiedAccess: FirQualifiedAccess) {
        if (!qualifiedAccess.safe) return
        val node = graphBuilder.enterSafeCall(qualifiedAccess).mergeIncomingFlow()
        val previousNode = node.alivePreviousNodes.first()
        val shouldFork: Boolean
        var flow = if (previousNode is ExitSafeCallNode) {
            shouldFork = false
            previousNode.alivePreviousNodes.getOrNull(1)?.flow ?: node.flow
        } else {
            shouldFork = true
            node.flow
        }
        qualifiedAccess.explicitReceiver?.let {
            val type = it.coneType
                .takeIf { it.isMarkedNullable }
                ?.withNullability(ConeNullability.NOT_NULL)
                ?: return@let

            when (val variable = variableStorage.getOrCreateVariable(it)) {
                is RealVariable -> {
                    if (shouldFork) {
                        flow = logicSystem.forkFlow(flow)
                    }
                    flow.addKnownInfo(variable has type)
                }
                is SyntheticVariable -> {
                    flow = logicSystem.approveStatementsInsideFlow(
                        flow,
                        variable notEq null,
                        shouldFork,
                        shouldRemoveSynthetics = true
                    )
                }
            }
        }

        node.flow = flow
    }

    fun exitQualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression) {
        graphBuilder.exitQualifiedAccessExpression(qualifiedAccessExpression).mergeIncomingFlow()
        exitSafeCall(qualifiedAccessExpression)
    }

    fun exitFunctionCall(functionCall: FirFunctionCall) {
        val node = graphBuilder.exitFunctionCall(functionCall).mergeIncomingFlow()
        if (functionCall.isBooleanNot()) {
            exitBooleanNot(functionCall, node)
        }
        processConditionalContract(functionCall)
        if (functionCall.safe) {
            exitSafeCall(functionCall)
        }
    }

    private fun exitSafeCall(qualifiedAccess: FirQualifiedAccess) {
        if (!qualifiedAccess.safe) return
        graphBuilder.exitSafeCall(qualifiedAccess).mergeIncomingFlow()
    }

    private fun processConditionalContract(functionCall: FirFunctionCall) {
        val contractDescription = (functionCall.symbol as? FirNamedFunctionSymbol)?.fir?.contractDescription ?: return
        val conditionalEffects = contractDescription.effects.filterIsInstance<ConeConditionalEffectDeclaration>()
        if (conditionalEffects.isEmpty()) return
        val argumentsMapping = createArgumentsMapping(functionCall) ?: return
        contractDescriptionVisitingMode = true
        graphBuilder.enterContract(functionCall).mergeIncomingFlow()
        val functionCallVariable = variableStorage.getOrCreateVariable(functionCall)
        for (conditionalEffect in conditionalEffects) {
            val fir = conditionalEffect.buildContractFir(argumentsMapping) ?: continue
            val effect = conditionalEffect.effect as? ConeReturnsEffectDeclaration ?: continue
            fir.transformSingle(components.transformer, ResolutionMode.ContextDependent)
            val argumentVariable = variableStorage.getOrCreateVariable(fir)
            val lastNode = graphBuilder.lastNode
            when (val value = effect.value) {
                ConeConstantReference.WILDCARD -> {
                    lastNode.flow = logicSystem.approveStatementsInsideFlow(
                        lastNode.flow,
                        argumentVariable eq true,
                        shouldForkFlow = false,
                        shouldRemoveSynthetics = true
                    )
                }

                is ConeBooleanConstantReference -> {
                    logicSystem.replaceConditionalVariableInStatements(
                        lastNode.flow,
                        argumentVariable,
                        functionCallVariable,
                        filter = { it.condition.condition == value.toCondition() }
                    )
                }

                ConeConstantReference.NOT_NULL, ConeConstantReference.NULL -> {
                    logicSystem.replaceConditionalVariableInStatements(
                        lastNode.flow,
                        argumentVariable,
                        functionCallVariable,
                        filter = { it.condition.condition == Condition.EqTrue },
                        transform = { Predicate(it.condition.variable, value.toCondition()) implies it.effect }
                    )
                }

                else -> throw IllegalArgumentException("Unsupported constant reference: $value")
            }
        }
        graphBuilder.exitContract(functionCall).mergeIncomingFlow()
        contractDescriptionVisitingMode = true
    }

    fun exitConstExpresion(constExpression: FirConstExpression<*>) {
        if (constExpression.resultType is FirResolvedTypeRef && !contractDescriptionVisitingMode) return
        graphBuilder.exitConstExpresion(constExpression).mergeIncomingFlow()
    }

    fun exitVariableDeclaration(variable: FirProperty) {
        val node = graphBuilder.exitVariableDeclaration(variable).mergeIncomingFlow()
        val initializer = variable.initializer ?: return

        when (val initializerVariable = variableStorage[initializer]) {
            is SyntheticVariable -> {
                /*
                 * That part is needed for cases like that:
                 *
                 *   val b = x is String
                 *   ...
                 *   if (b) {
                 *      x.length
                 *   }
                 */
                val propertyVariable = variableStorage.getOrCreateRealVariable(variable.symbol)
                logicSystem.replaceConditionalVariableInStatements(node.flow, initializerVariable, propertyVariable)
            }
            is RealVariable -> {
                if (initializerVariable.isStable) {
                    variableStorage.attachSymbolToVariable(variable.symbol, initializerVariable)
                } else {
                    val propertyVariable = variableStorage.getOrCreateRealVariable(variable.symbol)
                    node.flow.addLogicStatement((propertyVariable notEq null) implies (initializerVariable notEq null))
                }
            }
        }
    }

    fun exitVariableAssignment(assignment: FirVariableAssignment) {
        val node = graphBuilder.exitVariableAssignment(assignment).mergeIncomingFlow()
        val lValueSymbol: AbstractFirBasedSymbol<*> = assignment.lValue.symbol ?: error("left side of assignment should have symbol")
        val lhsVariable = variableStorage.getOrCreateRealVariable(lValueSymbol)
        val flow = node.flow
        flow.removeAllAboutVariable(lhsVariable)
        val rhsVariable = variableStorage.getOrCreateVariable(assignment.rValue)
        when (rhsVariable) {
            is SyntheticVariable -> {
                logicSystem.replaceConditionalVariableInStatements(flow, rhsVariable, lhsVariable)
                flow.addKnownInfo(lhsVariable has assignment.rValue.coneType)
            }
            is RealVariable -> {
                if (rhsVariable.isStable) {
                    variableStorage.attachSymbolToVariable(lValueSymbol, rhsVariable)
                } else {
                    flow.addLogicStatement((rhsVariable notEq null) implies (lhsVariable notEq null))
                }
            }
        }
    }

    fun exitThrowExceptionNode(throwExpression: FirThrowExpression) {
        graphBuilder.exitThrowExceptionNode(throwExpression).mergeIncomingFlow()
    }

    // ----------------------------------- Boolean operators -----------------------------------

    fun enterBinaryAnd(binaryLogicExpression: FirBinaryLogicExpression) {
        graphBuilder.enterBinaryAnd(binaryLogicExpression).mergeIncomingFlow()
    }

    fun exitLeftBinaryAndArgument(binaryLogicExpression: FirBinaryLogicExpression) {
        val (leftNode, rightNode) = graphBuilder.exitLeftBinaryAndArgument(binaryLogicExpression)
        exitLeftArgumentOfBinaryBooleanOperator(leftNode, rightNode, isAnd = true)
    }

    fun exitBinaryAnd(binaryLogicExpression: FirBinaryLogicExpression) {
        val node = graphBuilder.exitBinaryAnd(binaryLogicExpression)
        exitBinaryBooleanOperator(binaryLogicExpression, node, isAnd = true)
    }

    fun enterBinaryOr(binaryLogicExpression: FirBinaryLogicExpression) {
        graphBuilder.enterBinaryOr(binaryLogicExpression).mergeIncomingFlow()
    }

    fun exitLeftBinaryOrArgument(binaryLogicExpression: FirBinaryLogicExpression) {
        val (leftNode, rightNode) = graphBuilder.exitLeftBinaryOrArgument(binaryLogicExpression)
        exitLeftArgumentOfBinaryBooleanOperator(leftNode, rightNode, isAnd = false)
    }

    fun exitBinaryOr(binaryLogicExpression: FirBinaryLogicExpression) {
        val node = graphBuilder.exitBinaryOr(binaryLogicExpression)
        exitBinaryBooleanOperator(binaryLogicExpression, node, isAnd = false)
    }

    private fun exitLeftArgumentOfBinaryBooleanOperator(leftNode: CFGNode<*>, rightNode: CFGNode<*>, isAnd: Boolean) {
        val parentFlow = leftNode.alivePreviousNodes.first().flow
        leftNode.flow = logicSystem.forkFlow(parentFlow)
        val leftOperandVariable = variableStorage.getOrCreateVariable(leftNode.previousNodes.first().fir)
        rightNode.flow = logicSystem.approveStatementsInsideFlow(
            parentFlow,
            leftOperandVariable eq isAnd,
            shouldForkFlow = true,
            shouldRemoveSynthetics = false
        )
    }

    private fun exitBinaryBooleanOperator(
        binaryLogicExpression: FirBinaryLogicExpression,
        node: AbstractBinaryExitNode<*>,
        isAnd: Boolean
    ) {
        val bothEvaluated = isAnd
        val onlyLeftEvaluated = !bothEvaluated

        // Naming for all variables was chosen in assumption that we processing && expression
        val flowFromLeft = node.leftOperandNode.flow
        val flowFromRight = node.rightOperandNode.flow

        val flow = node.mergeIncomingFlow().flow

        val leftVariable = variableStorage.getOrCreateVariable(binaryLogicExpression.leftOperand)
        val rightVariable = variableStorage.getOrCreateVariable(binaryLogicExpression.rightOperand)
        val operatorVariable = variableStorage.getOrCreateVariable(binaryLogicExpression)

        val (conditionalFromLeft, conditionalFromRight, approvedFromRight) = logicSystem.collectInfoForBooleanOperator(
            flowFromLeft,
            leftVariable,
            flowFromRight,
            rightVariable
        )

        // left && right == True
        // left || right == False
        val approvedIfTrue: MutableKnownFacts = mutableMapOf()
        logicSystem.approvePredicate(approvedIfTrue, leftVariable eq bothEvaluated, conditionalFromLeft)
        logicSystem.approvePredicate(approvedIfTrue, rightVariable eq bothEvaluated, conditionalFromRight)
        approvedFromRight.forEach { (variable, info) ->
            approvedIfTrue.addInfo(variable, info)
        }
        approvedIfTrue.values.forEach { info ->
            flow.addLogicStatement((operatorVariable eq bothEvaluated) implies info)
        }

        // left && right == False
        // left || right == True
        val approvedIfFalse: MutableKnownFacts = mutableMapOf()
        val leftIsFalse = logicSystem.approvePredicate(leftVariable eq onlyLeftEvaluated, conditionalFromLeft)
        val rightIsFalse = logicSystem.approvePredicate(rightVariable eq onlyLeftEvaluated, conditionalFromRight)
        approvedIfFalse.mergeInfo(logicSystem.orForVerifiedFacts(leftIsFalse, rightIsFalse))
        approvedIfFalse.values.forEach { info ->
            flow.addLogicStatement((operatorVariable eq onlyLeftEvaluated) implies info)
        }

        node.flow = flow

        variableStorage.removeSyntheticVariable(leftVariable)
        variableStorage.removeSyntheticVariable(rightVariable)
    }


    private fun exitBooleanNot(functionCall: FirFunctionCall, node: FunctionCallNode) {
        val booleanExpressionVariable = variableStorage.getOrCreateVariable(node.previousNodes.first().fir)
        val variable = variableStorage.getOrCreateVariable(functionCall)
        logicSystem.replaceConditionalVariableInStatements(
            node.flow,
            booleanExpressionVariable,
            variable,
            transform = { it.invertCondition() }
        )
    }

    // ----------------------------------- Annotations -----------------------------------

    fun enterAnnotationCall(annotationCall: FirAnnotationCall) {
        graphBuilder.enterAnnotationCall(annotationCall).mergeIncomingFlow()
    }

    fun exitAnnotationCall(annotationCall: FirAnnotationCall) {
        graphBuilder.exitAnnotationCall(annotationCall).mergeIncomingFlow()
    }

    // ----------------------------------- Init block -----------------------------------

    fun enterInitBlock(initBlock: FirAnonymousInitializer) {
        graphBuilder.enterInitBlock(initBlock).mergeIncomingFlow()
    }

    fun exitInitBlock(initBlock: FirAnonymousInitializer) {
        graphBuilder.exitInitBlock(initBlock).mergeIncomingFlow()
    }

    // ------------------------------------------------------ Utils ------------------------------------------------------

    private var CFGNode<*>.flow: Flow
        get() = flowOnNodes.getValue(this.origin)
        set(value) {
            flowOnNodes[this.origin] = value
        }

    private val CFGNode<*>.origin: CFGNode<*> get() = if (this is StubNode) previousNodes.first() else this

    private fun <T : CFGNode<*>> T.mergeIncomingFlow(): T = this.also { node ->
        val previousFlows = node.alivePreviousNodes.map { it.flow }
        node.flow = logicSystem.joinFlow(previousFlows)
    }

    private fun Flow.addLogicStatement(statement: LogicStatement) {
        logicSystem.addLogicStatement(this, statement)
    }

    private fun Flow.addKnownInfo(info: DataFlowInfo) {
        logicSystem.addKnownInfo(this, info)
    }

    private fun Flow.removeAllAboutVariable(variable: RealVariable) {
        logicSystem.removeAllAboutVariable(this, variable)
    }
}