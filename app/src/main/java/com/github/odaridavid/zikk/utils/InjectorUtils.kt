package com.github.odaridavid.zikk.utils

/**
 *
 * Copyright 2020 David Odari
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *            http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 **/
import android.app.Activity
import android.app.Service
import com.github.odaridavid.zikk.ZikkApp
import com.github.odaridavid.zikk.di.AppComponent

internal val Activity.injector: AppComponent
    get() = (applicationContext as ZikkApp).appComponent

internal val Service.injector: AppComponent
    get() = (applicationContext as ZikkApp).appComponent