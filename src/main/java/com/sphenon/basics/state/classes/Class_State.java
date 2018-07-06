package com.sphenon.basics.state.classes;

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

public class Class_State implements State {

    public Class_State (CallContext context) {
    }

    public Class_State (CallContext context, String id) {
        this.id = id;
    }

    public Class_State (CallContext context, String id, String description) {
        this.id = id;
        this.description = description;
    }

    protected String id;

    public String getId (CallContext context) {
        return this.id;
    }

    public void setId (CallContext context, String id) {
        this.id = id;
    }

    protected String description;

    public String getDescription (CallContext context) {
        return this.description != null ? this.description : this.id;
    }

    public String defaultDescription (CallContext context) {
        return null;
    }

    public void setDescription (CallContext context, String description) {
        this.description = description;
    }

    public Class_State clone(CallContext context) {
        return new Class_State(context, this.id, this.description);
    }

    public boolean equals(CallContext context, State o) {
        if (o == null) { return false; }
        if ((o instanceof Class_State) == false) { return false; }
        Class_State other = (Class_State) o;

        if ((this.getId(context) == null) != (other.getId(context) == null)) { return false; }
        if (this.getId(context) != null && this.getId(context).equals(other.getId(context)) == false) { return false; }

        if ((this.getDescription(context) == null) != (other.getDescription(context) == null)) { return false; }
        if (this.getDescription(context) != null && this.getDescription(context).equals(other.getDescription(context)) == false) { return false; }

        return true;
    }
}
