/*
 * Copyright (C) 2015 Karumi.
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

package com.absensi.inuraini.dexter;

final class PermissionRationaleToken implements PermissionToken {

  private final DexterInstance dexterInstance;
  private boolean isTokenResolved = false;

  PermissionRationaleToken(DexterInstance dexterInstance) {
    this.dexterInstance = dexterInstance;
  }

  @Override public void continuePermissionRequest() {
    if (!isTokenResolved) {
      dexterInstance.onContinuePermissionRequest();
      isTokenResolved = true;
    }
  }

  @Override public void cancelPermissionRequest() {
    if (!isTokenResolved) {
      dexterInstance.onCancelPermissionRequest();
      isTokenResolved = true;
    }
  }
}
