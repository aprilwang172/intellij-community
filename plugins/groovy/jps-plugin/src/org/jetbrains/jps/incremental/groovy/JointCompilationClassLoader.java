// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.jps.incremental.groovy;

import com.intellij.util.lang.Resource;
import com.intellij.util.lang.UrlClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.ProtectionDomain;

/**
 * @author peter
 */
final class JointCompilationClassLoader extends UrlClassLoader {
  private static final boolean isParallelCapable = USE_PARALLEL_LOADING && registerAsParallelCapable();

  JointCompilationClassLoader(@NotNull UrlClassLoader.Builder builder) {
    super(builder, isParallelCapable);
  }

  @Override
  protected Class<?> _defineClass(String name, @NotNull Resource resource, @Nullable ProtectionDomain protectionDomain)
    throws IOException {
    try {
      return super._defineClass(name, resource, protectionDomain);
    }
    catch (NoClassDefFoundError e) {
      NoClassDefFoundError wrap = new NoClassDefFoundError(e.getMessage() + " needed for " + name);
      wrap.initCause(e);
      throw wrap;
    }
  }

  void resetCache() {
    getClassPath().reset(getFiles());
  }
}
