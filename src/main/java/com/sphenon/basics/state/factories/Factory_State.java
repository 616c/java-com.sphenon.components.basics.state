package com.sphenon.basics.state.factories;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;

import com.sphenon.basics.state.*;
import com.sphenon.basics.state.classes.*;

public class Factory_State {

    static public State construct(CallContext context, String id) {
        Factory_State factory = new Factory_State(context);
        factory.setId(context, id);
        return factory.create(context);
    }

    public Factory_State (CallContext context) {
    }

    protected String id;

    public String getId (CallContext context) {
        return this.id;
    }

    public void setId (CallContext context, String id) {
        this.id = id;
    }

    public State create(CallContext context) {
        Class_State instance = new Class_State(context);
        instance.setId(context, this.id);
        return instance;
    }
}
