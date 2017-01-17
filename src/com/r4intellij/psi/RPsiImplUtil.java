package com.r4intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.r4intellij.RElementGenerator;
import com.r4intellij.parsing.RElementTypes;
import com.r4intellij.psi.api.*;
import com.r4intellij.psi.references.ROperatorReference;
import com.r4intellij.psi.references.RReferenceImpl;
import com.r4intellij.typing.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alefas
 * @since 27/01/15.
 */
public class RPsiImplUtil {
    public static final TokenSet LEFT_ASSIGNMENTS = TokenSet.create(
            RElementTypes.R_LEFT_ASSIGN, RElementTypes.R_LEFT_COMPLEX_ASSIGN);
    public static final TokenSet RIGHT_ASSIGNMENTS = TokenSet.create(
            RElementTypes.R_RIGHT_ASSIGN, RElementTypes.R_RIGHT_COMPLEX_ASSIGN);
    public static final TokenSet RESERVED_WORDS = TokenSet.create(
            RElementTypes.R_IF, RElementTypes.R_ELSE, RElementTypes.R_REPEAT,
            RElementTypes.R_WHILE, RElementTypes.R_FUNCTION, RElementTypes.R_FOR,
            RElementTypes.R_IN, RElementTypes.R_NEXT, RElementTypes.R_BREAK);
    public static final TokenSet OPERATORS = TokenSet.create(
            RElementTypes.R_MINUS, RElementTypes.R_PLUS, RElementTypes.R_NOT, RElementTypes.R_TILDE, RElementTypes.R_HELP,
            RElementTypes.R_COLON, RElementTypes.R_MULT, RElementTypes.R_DIV, RElementTypes.R_EXP,
            RElementTypes.R_INFIX_OP, RElementTypes.R_LT, RElementTypes.R_GT, RElementTypes.R_EQEQ, RElementTypes.R_GE,
            RElementTypes.R_LE, RElementTypes.R_AND, RElementTypes.R_ANDAND, RElementTypes.R_OR, RElementTypes.R_OROR,
            RElementTypes.R_LEFT_ASSIGN, RElementTypes.R_RIGHT_ASSIGN, RElementTypes.R_LIST_SUBSET, RElementTypes.R_AT);


    public static String getName(ROperator binaryOperator) {
        return binaryOperator.getText();
    }


    public static ROperatorReference getReference(ROperator binaryOperator) {
        return new ROperatorReference(binaryOperator);
    }


    public static boolean isLeft(RAssignmentStatement assignment) {
        final ASTNode operator = assignment.getNode().findChildByType(LEFT_ASSIGNMENTS);
        return operator != null;
    }


    public static boolean isEqual(RAssignmentStatement assignment) {
        final ASTNode operator = assignment.getNode().findChildByType(RElementTypes.R_EQ);
        return operator != null;
    }


    public static boolean isRight(RAssignmentStatement assignment) {
        final ASTNode operator = assignment.getNode().findChildByType(RIGHT_ASSIGNMENTS);
        return operator != null;
    }


    public static RPsiElement getAssignedValue(RAssignmentStatement assignment) {
        PsiElement child;
        if (!assignment.isRight()) {
            child = assignment.getLastChild();
            while (child != null && !(child instanceof RExpression)) {
                if (child instanceof PsiErrorElement)
                    return null; // incomplete assignment operator can't be analyzed properly, bail out.
                child = child.getPrevSibling();
            }
        } else {
            child = assignment.getFirstChild();
            while (child != null && !(child instanceof RExpression)) {
                if (child instanceof PsiErrorElement)
                    return null; // incomplete assignment operator can't be analyzed properly, bail out.
                child = child.getNextSibling();
            }
        }
        return (RPsiElement) child;
    }


    public static PsiElement getAssignee(RAssignmentStatement assignment) {
        final ASTNode node = assignment.getNode();
        PsiElement child;
        if (!assignment.isRight()) {
            child = assignment.getFirstChild();
            while (child != null && !(child instanceof RExpression)) {
                if (child instanceof PsiErrorElement)
                    return null; // incomplete assignment operator can't be analyzed properly, bail out.
                child = child.getPrevSibling();
            }
        } else {
            child = assignment.getLastChild();
            while (child != null && !(child instanceof RExpression)) {
                if (child instanceof PsiErrorElement)
                    return null; // incomplete assignment operator can't be analyzed properly, bail out.
                child = child.getNextSibling();
            }
        }
        return child;
    }


    public static PsiElement setName(RAssignmentStatement assignment, String name) {
        ASTNode nameNode = assignment.getNameNode();
        if (nameNode == null) {
            return assignment;
        }
        final ASTNode oldNameIdentifier = nameNode.findChildByType(RElementTypes.R_IDENTIFIER);
        if (oldNameIdentifier != null) {
            final PsiFile dummyFile = RElementGenerator.createDummyFile(name, false, assignment.getProject());
            ASTNode identifier = dummyFile.getNode().getFirstChildNode().findChildByType(RElementTypes.R_IDENTIFIER);
            if (identifier != null) {
                nameNode.replaceChild(oldNameIdentifier, identifier);
            }
        }
        return assignment;
    }


    public static String getName(RAssignmentStatement assignment) {
        final ASTNode node = assignment.getNameNode();
        return node != null ? node.getText() : null;
    }


    public static ASTNode getNameNode(RAssignmentStatement assignment) {
        PsiElement assignee = assignment.getAssignee();
        return assignee == null ? null : assignee.getNode();
    }


    public static ASTNode getNameNode(RParameter parameter) {
        PsiElement identifier = parameter.getIdentifier();
        return identifier == null ? null : identifier.getNode();
    }


    public static String getName(RParameter parameter) {
        ASTNode node = parameter.getNameNode();
        return node == null ? null : node.getText();
    }


    public static PsiElement setName(RParameter parameter, String name) {
        final ASTNode oldNameIdentifier = parameter.getNameNode();
        if (oldNameIdentifier != null) {
            final PsiFile dummyFile = RElementGenerator.createDummyFile(name, false, parameter.getProject());
            ASTNode identifier = dummyFile.getNode().getFirstChildNode().findChildByType(RElementTypes.R_IDENTIFIER);
            if (identifier != null) {
                parameter.getNode().replaceChild(oldNameIdentifier, identifier);
            }
        }
        return parameter;
    }


    public static RReferenceImpl getReference(RReferenceExpression referenceExpression) {
        final PsiElement nextElement = PsiTreeUtil.skipSiblingsForward(referenceExpression, PsiWhiteSpace.class);
        if (nextElement != null && LEFT_ASSIGNMENTS.contains(nextElement.getNode().getElementType())) return null;
        final PsiElement prevElement = PsiTreeUtil.skipSiblingsBackward(referenceExpression, PsiWhiteSpace.class);
        if (prevElement != null && RIGHT_ASSIGNMENTS.contains(prevElement.getNode().getElementType())) return null;
        return new RReferenceImpl(referenceExpression);
    }


    public static String getTag(RMemberExpression memberExpression) {
        PsiElement identifier = memberExpression.getIdentifier();
        if (identifier != null) {
            return identifier.getText();
        }
        PsiElement name = memberExpression.getString();
        if (name != null) {
            return name.getText().substring(1, name.getText().length() - 1);
        }
        return "...";
    }


    public static String getTag(RAtExpression atExpression) {
        PsiElement identifier = atExpression.getIdentifier();
        if (identifier != null) {
            return identifier.getText();
        }
        PsiElement name = atExpression.getString();
        if (name != null) {
            return name.getText().substring(1, name.getText().length() - 1);
        }
        return "...";
    }


    @Nullable
    public static String getDocStringValue(@NotNull final RFunctionExpression functionExpression) {  //TODO: make stub-aware
        final RBlockExpression blockExpression = PsiTreeUtil.findChildOfType(functionExpression, RBlockExpression.class);
        if (blockExpression == null) return null;

        final List<PsiComment> comments = new ArrayList<PsiComment>();
        for (PsiElement sibling = blockExpression.getFirstChild(); sibling != null && !(sibling instanceof RExpression);
             sibling = sibling.getNextSibling()) {
            if (sibling instanceof PsiComment) {
                comments.add((PsiComment) sibling);
            }
        }
        if (comments.isEmpty()) return null;
        return getCommentText(comments);
    }


    @NotNull
    private static String getCommentText(@NotNull final List<PsiComment> comments) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (PsiComment comment : comments) {
            String string = comment.getText();
            stringBuilder.append(StringUtil.trimStart(string, "#")).append("\n");
        }
        return stringBuilder.toString();
    }


    @Nullable
    public static String getNamespace(RReferenceExpression referenceExpression) {
        final String text = referenceExpression.getText();
        final int namespaceIndex = text.indexOf("::");
        if (namespaceIndex > 0) {
            return text.substring(0, namespaceIndex);
        }
        return null;
    }


    public static String getName(RReferenceExpression referenceExpression) {
        final String text = referenceExpression.getText();
        final int namespaceIndex = text.indexOf("::");
        if (namespaceIndex > 0) {
            return text.substring(namespaceIndex + 2);
        }
        return text;
    }


    public static RType getType(RNaLiteralExpression na) {
        if (na.getNa() != null) {
            return RLogicalType.INSTANCE;
        }
        if (na.getNaCharacter() != null) {
            return RCharacterType.INSTANCE;
        }
        if (na.getNaComplex() != null) {
            return RComplexType.INSTANCE;
        }
        if (na.getNaInteger() != null) {
            return RIntegerType.INSTANCE;
        }
        if (na.getNaReal() != null) {
            return RNumericType.INSTANCE;
        }
        return RUnknownType.INSTANCE;
    }
}