// This is a generated file. Not intended for manual editing.
package com.r4intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.r4intellij.parsing.RElementTypes.*;
import com.r4intellij.psi.api.*;
import com.r4intellij.psi.references.RReferenceImpl;

public class RReferenceExpressionImpl extends RExpressionImpl implements RReferenceExpression {

  public RReferenceExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull RVisitor visitor) {
    visitor.visitReferenceExpression(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof RVisitor) accept((RVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return notNullChild(findChildByType(R_IDENTIFIER));
  }

  public RReferenceImpl getReference() {
    return RPsiImplUtil.getReference(this);
  }

  @Nullable
  public String getNamespace() {
    return RPsiImplUtil.getNamespace(this);
  }

  public String getName() {
    return RPsiImplUtil.getName(this);
  }

}
