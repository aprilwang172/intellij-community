package org.jetbrains.java.debugger;

import com.intellij.debugger.engine.evaluation.DefaultCodeFragmentFactory;
import com.intellij.debugger.engine.evaluation.TextWithImports;
import com.intellij.debugger.engine.evaluation.TextWithImportsImpl;
import com.intellij.debugger.ui.DebuggerExpressionComboBox;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragmentFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import com.intellij.xdebugger.impl.breakpoints.ui.XDebuggerComboBoxProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JavaDebuggerEditorsProvider extends XDebuggerEditorsProviderBase implements XDebuggerComboBoxProvider {
  @NotNull
  @Override
  public FileType getFileType() {
    return JavaFileType.INSTANCE;
  }

  @Override
  protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text, @Nullable PsiElement context, boolean isPhysical) {
    return JavaCodeFragmentFactory.getInstance(project).createExpressionCodeFragment(text, context, null, isPhysical);
  }

  @Override
  public XBreakpointCustomPropertiesPanel<XBreakpoint<?>> createConditionComboBoxPanel(Project project,
                                                                                       XDebuggerEditorsProvider debuggerEditorsProvider,
                                                                                       String historyId,
                                                                                       XSourcePosition sourcePosition) {
    return new ExpressionComboBoxPanel(project, historyId, sourcePosition) {
      @Override
      public void saveTo(@NotNull XBreakpoint<?> breakpoint) {
        TextWithImports text = myComboBox.getText();
        breakpoint.setConditionExpression(TextWithImportsImpl.toXExpression(text));
        if (!text.getText().isEmpty()) {
          myComboBox.addRecent(text);
        }
      }

      @Override
      public void loadFrom(@NotNull XBreakpoint<?> breakpoint) {
        TextWithImports text = TextWithImportsImpl.fromXExpression(breakpoint.getConditionExpression());
        if (text != null) {
          myComboBox.setText(text);
        }
      }
    };
  }

  @Override
  public XBreakpointCustomPropertiesPanel<XBreakpoint<?>> createLogExpressionComboBoxPanel(Project project,
                                                                                           XDebuggerEditorsProvider debuggerEditorsProvider,
                                                                                           String historyId,
                                                                                           XSourcePosition sourcePosition) {
    return new ExpressionComboBoxPanel(project, historyId, sourcePosition) {
      @Override
      public void saveTo(@NotNull XBreakpoint<?> breakpoint) {
        TextWithImports text = myComboBox.getText();
        breakpoint.setLogExpressionObject(myComboBox.isEnabled() ? TextWithImportsImpl.toXExpression(text) : null);
        if (text != null) {
          myComboBox.addRecent(text);
        }
      }

      @Override
      public void loadFrom(@NotNull XBreakpoint<?> breakpoint) {
        TextWithImports text = TextWithImportsImpl.fromXExpression(breakpoint.getLogExpressionObject());
        if (text != null) {
          myComboBox.setText(text);
        }
      }
    };
  }

  private abstract class ExpressionComboBoxPanel extends XBreakpointCustomPropertiesPanel<XBreakpoint<?>> {
    protected final DebuggerExpressionComboBox myComboBox;

    private ExpressionComboBoxPanel(Project project,
                                    String historyId,
                                    XSourcePosition sourcePosition) {
      PsiElement element = getContextElement(sourcePosition.getFile(), sourcePosition.getOffset(), project);
      myComboBox = new DebuggerExpressionComboBox(project, element, historyId, DefaultCodeFragmentFactory.getInstance());
      myComboBox.setContext(element);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
      return myComboBox;
    }

    @Override
    public void dispose() {
      myComboBox.dispose();
    }
  }
}