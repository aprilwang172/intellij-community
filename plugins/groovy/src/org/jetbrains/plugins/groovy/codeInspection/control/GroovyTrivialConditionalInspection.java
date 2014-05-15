/*
 * Copyright 2007-2008 Dave Griffith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.codeInspection.control;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspection;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspectionVisitor;
import org.jetbrains.plugins.groovy.codeInspection.GroovyFix;
import org.jetbrains.plugins.groovy.intentions.base.ErrorUtil;
import org.jetbrains.plugins.groovy.lang.psi.impl.utils.BoolUtils;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrConditionalExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;

public class GroovyTrivialConditionalInspection extends BaseInspection {

  @NotNull
  public String getDisplayName() {
    return "Redundant conditional expression";
  }

  @NotNull
  public String getGroupDisplayName() {
    return CONTROL_FLOW;
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new UnnecessaryConditionalExpressionVisitor();
  }

  public String buildErrorString(Object... args) {
    final GrConditionalExpression exp = (GrConditionalExpression) args[0];
    return "'" + exp.getText() + "' can be simplified to '" + calculateReplacementExpression(exp) + "'  #loc";
  }

  private static String calculateReplacementExpression(GrConditionalExpression exp) {
    final GrExpression thenExpression = exp.getThenBranch();
    final GrExpression elseExpression = exp.getElseBranch();
    final GrExpression condition = exp.getCondition();

    if (isFalse(thenExpression) && isTrue(elseExpression)) {
      return BoolUtils.getNegatedExpressionText(condition);
    } else {
      return condition.getText();
    }
  }

  public GroovyFix buildFix(PsiElement location) {
    return new TrivialConditionalFix();
  }

  private static class TrivialConditionalFix extends GroovyFix {

    @NotNull
    public String getName() {
      return "Simplify";
    }

    public void doFix(Project project, ProblemDescriptor descriptor)
        throws IncorrectOperationException {
      final GrConditionalExpression expression = (GrConditionalExpression) descriptor.getPsiElement();
      final String newExpression = calculateReplacementExpression(expression);
      replaceExpression(expression, newExpression);
    }
  }

  private static class UnnecessaryConditionalExpressionVisitor
      extends BaseInspectionVisitor {

    public void visitConditionalExpression(GrConditionalExpression exp) {
      super.visitConditionalExpression(exp);
      final GrExpression condition = exp.getCondition();
      final PsiType type = condition.getType();
      if (type == null || !(PsiType.BOOLEAN.isAssignableFrom(type))) {
        return;
      }

      if (ErrorUtil.containsError(exp)) return;

      final GrExpression thenExpression = exp.getThenBranch();
      if (thenExpression == null) {
        return;
      }
      final GrExpression elseExpression = exp.getElseBranch();
      if (elseExpression == null) {
        return;
      }
      if ((isFalse(thenExpression) && isTrue(elseExpression))
          || (isTrue(thenExpression) && isFalse(elseExpression))) {
        registerError(exp);
      }
    }
  }

  private static boolean isFalse(GrExpression expression) {
    @NonNls final String text = expression.getText();
    return "false".equals(text);
  }

  private static boolean isTrue(GrExpression expression) {
    @NonNls final String text = expression.getText();
    return "true".equals(text);
  }
}
