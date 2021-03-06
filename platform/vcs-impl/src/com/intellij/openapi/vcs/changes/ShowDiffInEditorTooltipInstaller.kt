// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.vcs.changes

import com.intellij.diff.editor.DiffRequestProcessorEditor
import com.intellij.diff.editor.DiffRequestProcessorEditorCustomizer
import com.intellij.diff.impl.DiffRequestProcessor
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.VcsBundle
import com.intellij.openapi.vcs.changes.ui.ActionToolbarGotItTooltip
import com.intellij.openapi.vcs.changes.ui.gearButton
import com.intellij.util.ui.update.DisposableUpdate
import com.intellij.util.ui.update.MergingUpdateQueue

class ShowDiffInEditorTooltipInstaller : DiffRequestProcessorEditorCustomizer {

  override fun customize(editor: DiffRequestProcessorEditor) {
    val diffProcessor = editor.processor
    ShowDiffInEditorTabTooltipHolder(editor, diffProcessor)
  }
}

private class ShowDiffInEditorTabTooltipHolder(disposable: Disposable,
                                               private val diffProcessor: DiffRequestProcessor) :
  EditorDiffPreviewFilesListener, Disposable {

  companion object {
    const val TOOLTIP_ID = "show.diff.in.editor"
  }

  /**
   * In case of multiple show tooltip request coming from different listeners, [MergingUpdateQueue] will help here to ensure that only one tooltip will be shown
   */
  private val notificationQueue = MergingUpdateQueue("DiffRequestNotificationQueue", 500, true, null, this)

  init {
    Disposer.register(disposable, this)
    ApplicationManager.getApplication().messageBus.connect(this).subscribe(EditorDiffPreviewFilesListener.TOPIC, this)
  }

  override fun shouldOpenInNewWindowChanged(shouldOpenInNewWindow: Boolean) {
    if (shouldOpenInNewWindow) {
      showGotItTooltip()
    }
  }

  private fun showGotItTooltip() = notificationQueue.queue(DisposableUpdate.createDisposable(this, TOOLTIP_ID, {
    val targetComponent = diffProcessor.contentPanel.targetComponent
    ActionToolbarGotItTooltip(TOOLTIP_ID, VcsBundle.message("show.diff.in.editor.tab.got.it.tooltip"),
                              this, diffProcessor.toolbar, gearButton, targetComponent)
  }))

  override fun dispose() {}
}
