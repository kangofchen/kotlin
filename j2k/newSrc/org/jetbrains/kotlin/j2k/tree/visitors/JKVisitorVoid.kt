package org.jetbrains.kotlin.j2k.tree.visitors

import org.jetbrains.kotlin.j2k.tree.*

interface JKVisitorVoid : JKVisitor<Unit, Nothing?> {
    fun visitElement(element: JKElement) 
    override fun visitElement(element: JKElement, data: Nothing?) = visitElement(element)
    fun visitClass(klass: JKClass) = visitDeclaration(klass, null)
    override fun visitClass(klass: JKClass, data: Nothing?) = visitClass(klass)
    fun visitStatement(statement: JKStatement) = visitElement(statement, null)
    override fun visitStatement(statement: JKStatement, data: Nothing?) = visitStatement(statement)
    fun visitExpression(expression: JKExpression) = visitStatement(expression, null)
    override fun visitExpression(expression: JKExpression, data: Nothing?) = visitExpression(expression)
    fun visitBinaryExpression(binaryExpression: JKBinaryExpression) = visitExpression(binaryExpression, null)
    override fun visitBinaryExpression(binaryExpression: JKBinaryExpression, data: Nothing?) = visitBinaryExpression(binaryExpression)
    fun visitUnaryExpression(unaryExpression: JKUnaryExpression) = visitExpression(unaryExpression, null)
    override fun visitUnaryExpression(unaryExpression: JKUnaryExpression, data: Nothing?) = visitUnaryExpression(unaryExpression)
    fun visitPrefixExpression(prefixExpression: JKPrefixExpression) = visitUnaryExpression(prefixExpression, null)
    override fun visitPrefixExpression(prefixExpression: JKPrefixExpression, data: Nothing?) = visitPrefixExpression(prefixExpression)
    fun visitPostfixExpression(postfixExpression: JKPostfixExpression) = visitUnaryExpression(postfixExpression, null)
    override fun visitPostfixExpression(postfixExpression: JKPostfixExpression, data: Nothing?) = visitPostfixExpression(postfixExpression)
    fun visitQualifiedExpression(qualifiedExpression: JKQualifiedExpression) = visitExpression(qualifiedExpression, null)
    override fun visitQualifiedExpression(qualifiedExpression: JKQualifiedExpression, data: Nothing?) = visitQualifiedExpression(qualifiedExpression)
    fun visitMethodCallExpression(methodCallExpression: JKMethodCallExpression) = visitExpression(methodCallExpression, null)
    override fun visitMethodCallExpression(methodCallExpression: JKMethodCallExpression, data: Nothing?) = visitMethodCallExpression(methodCallExpression)
    fun visitFieldAccessExpression(fieldAccessExpression: JKFieldAccessExpression) = visitExpression(fieldAccessExpression, null)
    override fun visitFieldAccessExpression(fieldAccessExpression: JKFieldAccessExpression, data: Nothing?) = visitFieldAccessExpression(fieldAccessExpression)
    fun visitArrayAccessExpression(arrayAccessExpression: JKArrayAccessExpression) = visitExpression(arrayAccessExpression, null)
    override fun visitArrayAccessExpression(arrayAccessExpression: JKArrayAccessExpression, data: Nothing?) = visitArrayAccessExpression(arrayAccessExpression)
    fun visitParenthesizedExpression(parenthesizedExpression: JKParenthesizedExpression) = visitExpression(parenthesizedExpression, null)
    override fun visitParenthesizedExpression(parenthesizedExpression: JKParenthesizedExpression, data: Nothing?) = visitParenthesizedExpression(parenthesizedExpression)
    fun visitTypeCastExpression(typeCastExpression: JKTypeCastExpression) = visitExpression(typeCastExpression, null)
    override fun visitTypeCastExpression(typeCastExpression: JKTypeCastExpression, data: Nothing?) = visitTypeCastExpression(typeCastExpression)
    fun visitExpressionList(expressionList: JKExpressionList) = visitElement(expressionList, null)
    override fun visitExpressionList(expressionList: JKExpressionList, data: Nothing?) = visitExpressionList(expressionList)
    fun visitReference(reference: JKReference) = visitElement(reference, null)
    override fun visitReference(reference: JKReference, data: Nothing?) = visitReference(reference)
    fun visitMethodReference(methodReference: JKMethodReference) = visitReference(methodReference, null)
    override fun visitMethodReference(methodReference: JKMethodReference, data: Nothing?) = visitMethodReference(methodReference)
    fun visitFieldReference(fieldReference: JKFieldReference) = visitReference(fieldReference, null)
    override fun visitFieldReference(fieldReference: JKFieldReference, data: Nothing?) = visitFieldReference(fieldReference)
    fun visitClassReference(classReference: JKClassReference) = visitReference(classReference, null)
    override fun visitClassReference(classReference: JKClassReference, data: Nothing?) = visitClassReference(classReference)
    fun visitType(type: JKType) = visitElement(type, null)
    override fun visitType(type: JKType, data: Nothing?) = visitType(type)
    fun visitClassType(classType: JKClassType) = visitType(classType, null)
    override fun visitClassType(classType: JKClassType, data: Nothing?) = visitClassType(classType)
    fun visitOperatorIdentifier(operatorIdentifier: JKOperatorIdentifier) = visitIdentifier(operatorIdentifier, null)
    override fun visitOperatorIdentifier(operatorIdentifier: JKOperatorIdentifier, data: Nothing?) = visitOperatorIdentifier(operatorIdentifier)
    fun visitQualificationIdentifier(qualificationIdentifier: JKQualificationIdentifier) = visitIdentifier(qualificationIdentifier, null)
    override fun visitQualificationIdentifier(qualificationIdentifier: JKQualificationIdentifier, data: Nothing?) = visitQualificationIdentifier(qualificationIdentifier)
    fun visitLoop(loop: JKLoop) = visitStatement(loop, null)
    override fun visitLoop(loop: JKLoop, data: Nothing?) = visitLoop(loop)
    fun visitDeclaration(declaration: JKDeclaration) = visitElement(declaration, null)
    override fun visitDeclaration(declaration: JKDeclaration, data: Nothing?) = visitDeclaration(declaration)
    fun visitBlock(block: JKBlock) = visitElement(block, null)
    override fun visitBlock(block: JKBlock, data: Nothing?) = visitBlock(block)
    fun visitIdentifier(identifier: JKIdentifier) = visitElement(identifier, null)
    override fun visitIdentifier(identifier: JKIdentifier, data: Nothing?) = visitIdentifier(identifier)
    fun visitNameIdentifier(nameIdentifier: JKNameIdentifier) = visitIdentifier(nameIdentifier, null)
    override fun visitNameIdentifier(nameIdentifier: JKNameIdentifier, data: Nothing?) = visitNameIdentifier(nameIdentifier)
    fun visitLiteralExpression(literalExpression: JKLiteralExpression) = visitExpression(literalExpression, null)
    override fun visitLiteralExpression(literalExpression: JKLiteralExpression, data: Nothing?) = visitLiteralExpression(literalExpression)
    fun visitModifierList(modifierList: JKModifierList) = visitElement(modifierList, null)
    override fun visitModifierList(modifierList: JKModifierList, data: Nothing?) = visitModifierList(modifierList)
    fun visitModifier(modifier: JKModifier) = visitElement(modifier, null)
    override fun visitModifier(modifier: JKModifier, data: Nothing?) = visitModifier(modifier)
    fun visitAccessModifier(accessModifier: JKAccessModifier) = visitModifier(accessModifier, null)
    override fun visitAccessModifier(accessModifier: JKAccessModifier, data: Nothing?) = visitAccessModifier(accessModifier)
    fun visitValueArgument(valueArgument: JKValueArgument) = visitElement(valueArgument, null)
    override fun visitValueArgument(valueArgument: JKValueArgument, data: Nothing?) = visitValueArgument(valueArgument)
    fun visitStringLiteralExpression(stringLiteralExpression: JKStringLiteralExpression) = visitLiteralExpression(stringLiteralExpression, null)
    override fun visitStringLiteralExpression(stringLiteralExpression: JKStringLiteralExpression, data: Nothing?) = visitStringLiteralExpression(stringLiteralExpression)
    fun visitModalityModifier(modalityModifier: JKModalityModifier) = visitModifier(modalityModifier, null)
    override fun visitModalityModifier(modalityModifier: JKModalityModifier, data: Nothing?) = visitModalityModifier(modalityModifier)
    fun visitMethod(method: JKMethod) = visitDeclaration(method, null)
    override fun visitMethod(method: JKMethod, data: Nothing?) = visitMethod(method)
    fun visitExpressionStatement(expressionStatement: JKExpressionStatement) = visitStatement(expressionStatement, null)
    override fun visitExpressionStatement(expressionStatement: JKExpressionStatement, data: Nothing?) = visitExpressionStatement(expressionStatement)
    fun visitJavaField(javaField: JKJavaField) = visitDeclaration(javaField, null)
    override fun visitJavaField(javaField: JKJavaField, data: Nothing?) = visitJavaField(javaField)
    fun visitJavaMethod(javaMethod: JKJavaMethod) = visitMethod(javaMethod, null)
    override fun visitJavaMethod(javaMethod: JKJavaMethod, data: Nothing?) = visitJavaMethod(javaMethod)
    fun visitJavaForLoop(javaForLoop: JKJavaForLoop) = visitLoop(javaForLoop, null)
    override fun visitJavaForLoop(javaForLoop: JKJavaForLoop, data: Nothing?) = visitJavaForLoop(javaForLoop)
    fun visitJavaAssignmentExpression(javaAssignmentExpression: JKJavaAssignmentExpression) = visitExpression(javaAssignmentExpression, null)
    override fun visitJavaAssignmentExpression(javaAssignmentExpression: JKJavaAssignmentExpression, data: Nothing?) = visitJavaAssignmentExpression(javaAssignmentExpression)
    fun visitJavaPrimitiveType(javaPrimitiveType: JKJavaPrimitiveType) = visitType(javaPrimitiveType, null)
    override fun visitJavaPrimitiveType(javaPrimitiveType: JKJavaPrimitiveType, data: Nothing?) = visitJavaPrimitiveType(javaPrimitiveType)
    fun visitJavaArrayType(javaArrayType: JKJavaArrayType) = visitType(javaArrayType, null)
    override fun visitJavaArrayType(javaArrayType: JKJavaArrayType, data: Nothing?) = visitJavaArrayType(javaArrayType)
    fun visitJavaOperatorIdentifier(javaOperatorIdentifier: JKJavaOperatorIdentifier) = visitOperatorIdentifier(javaOperatorIdentifier, null)
    override fun visitJavaOperatorIdentifier(javaOperatorIdentifier: JKJavaOperatorIdentifier, data: Nothing?) = visitJavaOperatorIdentifier(javaOperatorIdentifier)
    fun visitJavaQualificationIdentifier(javaQualificationIdentifier: JKJavaQualificationIdentifier) = visitQualificationIdentifier(javaQualificationIdentifier, null)
    override fun visitJavaQualificationIdentifier(javaQualificationIdentifier: JKJavaQualificationIdentifier, data: Nothing?) = visitJavaQualificationIdentifier(javaQualificationIdentifier)
    fun visitJavaMethodCallExpression(javaMethodCallExpression: JKJavaMethodCallExpression) = visitMethodCallExpression(javaMethodCallExpression, null)
    override fun visitJavaMethodCallExpression(javaMethodCallExpression: JKJavaMethodCallExpression, data: Nothing?) = visitJavaMethodCallExpression(javaMethodCallExpression)
    fun visitJavaFieldAccessExpression(javaFieldAccessExpression: JKJavaFieldAccessExpression) = visitFieldAccessExpression(javaFieldAccessExpression, null)
    override fun visitJavaFieldAccessExpression(javaFieldAccessExpression: JKJavaFieldAccessExpression, data: Nothing?) = visitJavaFieldAccessExpression(javaFieldAccessExpression)
    fun visitJavaNewExpression(javaNewExpression: JKJavaNewExpression) = visitExpression(javaNewExpression, null)
    override fun visitJavaNewExpression(javaNewExpression: JKJavaNewExpression, data: Nothing?) = visitJavaNewExpression(javaNewExpression)
    fun visitJavaMethodReference(javaMethodReference: JKJavaMethodReference) = visitMethodReference(javaMethodReference, null)
    override fun visitJavaMethodReference(javaMethodReference: JKJavaMethodReference, data: Nothing?) = visitJavaMethodReference(javaMethodReference)
    fun visitJavaFieldReference(javaFieldReference: JKJavaFieldReference) = visitFieldReference(javaFieldReference, null)
    override fun visitJavaFieldReference(javaFieldReference: JKJavaFieldReference, data: Nothing?) = visitJavaFieldReference(javaFieldReference)
    fun visitJavaClassReference(javaClassReference: JKJavaClassReference) = visitClassReference(javaClassReference, null)
    override fun visitJavaClassReference(javaClassReference: JKJavaClassReference, data: Nothing?) = visitJavaClassReference(javaClassReference)
    fun visitJavaAccessModifier(javaAccessModifier: JKJavaAccessModifier) = visitAccessModifier(javaAccessModifier, null)
    override fun visitJavaAccessModifier(javaAccessModifier: JKJavaAccessModifier, data: Nothing?) = visitJavaAccessModifier(javaAccessModifier)
    fun visitJavaModifier(javaModifier: JKJavaModifier) = visitModifier(javaModifier, null)
    override fun visitJavaModifier(javaModifier: JKJavaModifier, data: Nothing?) = visitJavaModifier(javaModifier)
    fun visitJavaNewEmptyArray(javaNewEmptyArray: JKJavaNewEmptyArray) = visitExpression(javaNewEmptyArray, null)
    override fun visitJavaNewEmptyArray(javaNewEmptyArray: JKJavaNewEmptyArray, data: Nothing?) = visitJavaNewEmptyArray(javaNewEmptyArray)
    fun visitJavaNewArray(javaNewArray: JKJavaNewArray) = visitExpression(javaNewArray, null)
    override fun visitJavaNewArray(javaNewArray: JKJavaNewArray, data: Nothing?) = visitJavaNewArray(javaNewArray)
    fun visitJavaLiteralExpression(javaLiteralExpression: JKJavaLiteralExpression) = visitLiteralExpression(javaLiteralExpression, null)
    override fun visitJavaLiteralExpression(javaLiteralExpression: JKJavaLiteralExpression, data: Nothing?) = visitJavaLiteralExpression(javaLiteralExpression)
    fun visitKtFun(ktFun: JKKtFun) = visitDeclaration(ktFun, null)
    override fun visitKtFun(ktFun: JKKtFun, data: Nothing?) = visitKtFun(ktFun)
    fun visitKtConstructor(ktConstructor: JKKtConstructor) = visitDeclaration(ktConstructor, null)
    override fun visitKtConstructor(ktConstructor: JKKtConstructor, data: Nothing?) = visitKtConstructor(ktConstructor)
    fun visitKtPrimaryConstructor(ktPrimaryConstructor: JKKtPrimaryConstructor) = visitKtConstructor(ktPrimaryConstructor, null)
    override fun visitKtPrimaryConstructor(ktPrimaryConstructor: JKKtPrimaryConstructor, data: Nothing?) = visitKtPrimaryConstructor(ktPrimaryConstructor)
    fun visitKtAssignmentStatement(ktAssignmentStatement: JKKtAssignmentStatement) = visitStatement(ktAssignmentStatement, null)
    override fun visitKtAssignmentStatement(ktAssignmentStatement: JKKtAssignmentStatement, data: Nothing?) = visitKtAssignmentStatement(ktAssignmentStatement)
    fun visitKtCall(ktCall: JKKtCall) = visitMethodCallExpression(ktCall, null)
    override fun visitKtCall(ktCall: JKKtCall, data: Nothing?) = visitKtCall(ktCall)
    fun visitKtProperty(ktProperty: JKKtProperty) = visitDeclaration(ktProperty, null)
    override fun visitKtProperty(ktProperty: JKKtProperty, data: Nothing?) = visitKtProperty(ktProperty)
    fun visitKtFunction(ktFunction: JKKtFunction) = visitDeclaration(ktFunction, null)
    override fun visitKtFunction(ktFunction: JKKtFunction, data: Nothing?) = visitKtFunction(ktFunction)
    fun visitKtModifier(ktModifier: JKKtModifier) = visitModifier(ktModifier, null)
    override fun visitKtModifier(ktModifier: JKKtModifier, data: Nothing?) = visitKtModifier(ktModifier)
    fun visitKtQualificationIdentifier(ktQualificationIdentifier: JKKtQualificationIdentifier) = visitQualificationIdentifier(ktQualificationIdentifier, null)
    override fun visitKtQualificationIdentifier(ktQualificationIdentifier: JKKtQualificationIdentifier, data: Nothing?) = visitKtQualificationIdentifier(ktQualificationIdentifier)
    fun visitKtMethodCallExpression(ktMethodCallExpression: JKKtMethodCallExpression) = visitMethodCallExpression(ktMethodCallExpression, null)
    override fun visitKtMethodCallExpression(ktMethodCallExpression: JKKtMethodCallExpression, data: Nothing?) = visitKtMethodCallExpression(ktMethodCallExpression)
    fun visitKtFieldAccessExpression(ktFieldAccessExpression: JKKtFieldAccessExpression) = visitFieldAccessExpression(ktFieldAccessExpression, null)
    override fun visitKtFieldAccessExpression(ktFieldAccessExpression: JKKtFieldAccessExpression, data: Nothing?) = visitKtFieldAccessExpression(ktFieldAccessExpression)
    fun visitKtLiteralExpression(ktLiteralExpression: JKKtLiteralExpression) = visitLiteralExpression(ktLiteralExpression, null)
    override fun visitKtLiteralExpression(ktLiteralExpression: JKKtLiteralExpression, data: Nothing?) = visitKtLiteralExpression(ktLiteralExpression)
    fun visitKtOperatorIdentifier(ktOperatorIdentifier: JKKtOperatorIdentifier) = visitOperatorIdentifier(ktOperatorIdentifier, null)
    override fun visitKtOperatorIdentifier(ktOperatorIdentifier: JKKtOperatorIdentifier, data: Nothing?) = visitKtOperatorIdentifier(ktOperatorIdentifier)
}
