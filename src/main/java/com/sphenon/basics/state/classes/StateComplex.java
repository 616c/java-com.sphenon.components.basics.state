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
import com.sphenon.basics.exception.*;
import com.sphenon.sm.tsm.*;

import com.sphenon.basics.state.*;

import com.sphenon.ui.core.*;
import com.sphenon.ui.annotations.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

@UIId         ("state")
@UIName       ("State")
@UIClassifier ("State")
public class StateComplex implements State, TSMMapped, UIEquipped {

    public StateComplex (CallContext context) {
    }

    /**
       Creates new State with variable definitions and current values.

       @param variables An array of variable definitions, each definition has the format

                             name [ : [ value1 [ / description1 ] [ , value2 [ / description2 ] ... ] ] ]

                        Examples:
                             "mystate"
                             "mystate:one,two,three"
                             "color:red,green,blue"
                             "color:red/Like a rose,green/As hope,blue/Like beautiful eyes"

       @param values    An array of current values, if valid values are defined in the variables
                        array (e.g. "red,green,blue" for "color", then these values here must
                        be contained in that lists, this is checked
     */
    public StateComplex (CallContext context, String[] variables, String[] values) {
        this(context, computeValidValues(context, variables, values), values);
    }

    public StateComplex (CallContext context, Specification specification, String[] values) {
        this(context, specification, values, null, null);
    }

    public StateComplex (CallContext context, Specification specification, String[] values, String[] sub_state_variables, State[] sub_states) {
        this.specification = specification;
        this.variables     = specification.variables;
        this.valid_values  = specification.valid_values;
        this.values        = new String[this.variables.length];
        this.descriptions  = new String[this.variables.length];
        this.sub_state_variables = sub_state_variables;
        this.sub_states          = sub_states;

        if (values != null || specification.initial_values != null) {
            for (int i=0; i<this.variables.length; i++) {
                if (    values != null
                     && values.length > i
                     && values[i] != null) {
                    this.setValue(context, this.variables[i], values[i], i);
                } else if (    specification.initial_values != null
                            && specification.initial_values.length > i
                               && specification.initial_values[i] != null) {
                    this.setValue(context, this.variables[i], specification.initial_values[i], i);
                }
            }
        }
    }

    static public class Specification {
        public String[][][] valid_values;
        public String[]     initial_values;
        public String[]     variables;
    }

    static public Specification computeValidValues(CallContext context, String[] variables) {
        return computeValidValues(context, variables, null);
    }

    static public Specification computeValidValues(CallContext context, String[] variables, String[] values_for_reference) {
        Specification s = new Specification();
        if (variables == null) {
            variables = new String[values_for_reference == null ? 0 : values_for_reference.length];
            if (values_for_reference != null) {
                for (int i=0; i<values_for_reference.length; i++) {
                    variables[i] = Integer.toString(i);
                }
            }
            s.variables = variables;
        } else {
            s.variables = variables;
            for (int i=0; i<s.variables.length; i++) {
                String[] v = s.variables[i].split(":");
                if (v.length > 1) {
                    if (s.valid_values == null) {
                        s.valid_values = new String[s.variables.length][][];
                    }
                    String[] vvs = v[1].split(",");
                    s.variables[i] = v[0];
                    s.valid_values[i] = new String[vvs.length][];
                    for (int j=0; j<vvs.length; j++) {
                        s.valid_values[i][j] = new String[2];
                        String[] vv = vvs[j].split("/");
                        s.valid_values[i][j][0] = vv[0];
                        s.valid_values[i][j][1] = vv.length > 1 ? vv[1] : null;
                    }
                }
                if (v.length > 2) {
                    if (s.initial_values == null) {
                        s.initial_values = new String[s.variables.length];
                    }
                    s.initial_values[i] = v[2];
                }
            }
        }
        return s;
    }

    public StateComplex (CallContext context, String... variables) {
        this(context, variables, null);
    }

    public StateComplex setSubStates(CallContext context, Object... arguments) {
        if (arguments == null) {
            this.sub_state_variables = null;
            this.sub_states          = null;
            return this;
        }

        int l = arguments.length / 2;
        this.sub_state_variables = new String[l];
        this.sub_states          = new State[l];
        for (int i=0, j=0; i<l; i++, j+=2) {
            this.sub_state_variables[i] = (String) arguments[j];
            this.sub_states[i]          = (State)  arguments[j+1];
        }

        return this;
    }

    /**
       Creates new State with variable definitions and current values from a single string

       @param all_in_one The format is

                             name1 [ : [ value11 [ / description11 ] [ , value12 [ / description12 ] ... ] ] ] ;
                             name2 [ : [ value21 [ / description21 ] [ , value22 [ / description22 ] ... ] ] ] ;
                             ...
                             #
                             initialvalue1 ; initialvalue2 ; ...

                        Example:
                             "color:red/Like a rose,green/As hope,blue/Like beautiful eyes#red"

       @return new complex state instance
     */
    static public StateComplex create(CallContext context, String all_in_one) {
        String[] varval = all_in_one.split("#");
        return new StateComplex(context,
                                varval[0].isEmpty() ? null : varval[0].split(";"),
                                varval.length <= 1 || varval[1].isEmpty() ? null : varval[1].split(";"));
    }

    protected Specification specification;

    public Specification getSpecification (CallContext context) {
        return this.specification;
    }

    protected String[] variables;

    public String[] getVariables (CallContext context) {
        return this.variables;
    }

    protected String[][][] valid_values;

    public String[][][] getValidValues (CallContext context) {
        return this.valid_values;
    }

    protected String[] values;

    public String[] getValues (CallContext context) {
        return this.values;
    }

    public List<String[]> getValues (CallContext context, String pattern) {
        String[] all_names  = getVariables(context);
        String[] all_values = getValues(context);
        List<String[]> matching = new ArrayList<String[]>();
        int size = (all_names == null ? 0 : all_names.length);
        for (int i=0; i<size; i++) {
            if (pattern == null || all_names[i].matches(pattern)) {
                matching.add(new String[] { all_names[i], all_values[i] });
            }
        }
        return matching;
    }

    protected String[] descriptions;

    public String[] getDescriptions (CallContext context) {
        return this.descriptions;
    }

    protected String[] sub_state_variables;

    public String[] getSubStateVariables (CallContext context) {
        return this.sub_state_variables;
    }

    protected State[] sub_states;

    public State[] getSubStates (CallContext context) {
        return this.sub_states;
    }

    protected String doGetValue (CallContext context, String variable, boolean throw_exception) {
        int dot = variable.indexOf('.');
        if (dot == -1 && this.variables != null) {
            for (int i=0; i<this.variables.length; i++) {
                if (this.variables[i].equals(variable)) {
                    return this.values[i];
                }
            }
        }
        if (dot != -1 && this.sub_state_variables != null) {
            String ssv = variable.substring(0, dot);
            for (int i=0; i<this.sub_state_variables.length; i++) {
                if (this.sub_state_variables[i].equals(ssv)) {
                    String v = variable.substring(dot+1);
                    State substate = this.sub_states[i];
                    return (  substate instanceof StateComplex ? ((StateComplex) substate).tryGetValue(context, v)
                            : "main".equals(v)                 ? substate.getId(context)
                            :                                    null);
                }
            }
        }
        if (throw_exception) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot get variable '%(variable)' in complex state '%(id)' (no such variable)", "variable", variable, "id", this.getId(context));
            throw (ExceptionPreConditionViolation) null; // compiler insists
        } else {
            return null;
        }
    }

    public String tryGetValue (CallContext context, String variable) {
        return doGetValue (context, variable, false);
    }

    public String getValue (CallContext context, String variable) {
        return doGetValue (context, variable, true);
    }

    public void setValue (CallContext context, String variable, String value) {
        if (this.variables != null) {
            for (int i=0; i<this.variables.length; i++) {
                if (this.variables[i].equals(variable)) {
                    this.setValue (context, variable, value, i);
                    return;
                }
            }
        }
        CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot set variable '%(variable)' to '%(value)' in complex state '%(id)' (no such variable)", "variable", variable, "value", value, "id", this.getId(context));
        throw (ExceptionPreConditionViolation) null; // compiler insists
    }

    protected void setValue (CallContext context, String variable, String value, int i) {
        String description = null;
        if (this.valid_values != null) {
            String[][] vvs = this.valid_values[i];
            for (int j=0; description == null && j<vvs.length; j++) {
                if (this.valid_values[i][j][0].equals(value)) {
                    description = (this.valid_values[i][j][1] != null ? this.valid_values[i][j][1] : (variable + "=" + value));
                }
            }
            if (description == null) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot set variable '%(variable)' to '%(value)' in complex state '%(id)' (invalid value)", "variable", variable, "value", value, "id", this.getId(context));
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
        }
        this.values[i]       = value;
        this.descriptions[i] = description;
    }

    public String getId (CallContext context) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        if (this.variables != null) {
            for (int i=0; i<this.variables.length; i++) {
                if (first == false) { sb.append(","); }
                first = false;
                sb.append(this.variables[i] + "=" + this.values[i]);
            }
        }
        if (this.sub_state_variables != null) {
            for (int i=0; i<this.sub_state_variables.length; i++) {
                if (first == false) { sb.append(","); }
                first = false;
                sb.append(this.sub_state_variables[i] + "[");
                sb.append(this.sub_states[i].getId(context));
                sb.append("]");
            }
        }
        return sb.toString();
    }

    public String toString() {
        return "[State: '" + this.getId(RootContext.getFallbackCallContext()) + "']";
    }

    public String getDescription (CallContext context) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        if (this.variables != null) {
            for (int i=0; i<this.variables.length; i++) {
                if (first == false) { sb.append(","); }
                first = false;
                sb.append(this.descriptions[i]);
            }
        }
        if (this.sub_state_variables != null) {
            for (int i=0; i<this.sub_state_variables.length; i++) {
                if (first == false) { sb.append(","); }
                first = false;
                sb.append(this.sub_state_variables[i] + "[");
                sb.append(this.sub_states[i].getDescription(context));
                sb.append("]");
            }
        }
        return sb.toString();
    }

    public StateComplex clone(CallContext context) {
        State[] sub_states = this.getSubStates(context);
        State[] cloned_sub_states = null;
        if (sub_states != null) {
            cloned_sub_states = new State[sub_states.length];
            for (int i=0; i<sub_states.length; i++) {
                cloned_sub_states[i] = sub_states[i].clone(context);
            }
        }
        return new StateComplex(context, this.getSpecification(context), this.getValues(context).clone(), this.getSubStateVariables(context), cloned_sub_states);
    }

    public boolean equals(CallContext context, State o) {
        if (o == null) { return false; }
        if ((o instanceof StateComplex) == false) { return false; }
        StateComplex other = (StateComplex) o;

        if ((this.getVariables(context) == null) != (other.getVariables(context) == null)) { return false; }
        if ((this.getValues(context) == null) != (other.getValues(context) == null)) { return false; }

        if ((this.getSubStateVariables(context) == null) != (other.getSubStateVariables(context) == null)) { return false; }
        if ((this.getSubStates(context) == null) != (other.getSubStates(context) == null)) { return false; }

        if ((this.getVariables(context) != null) && (other.getVariables(context) != null)) {
            if (this.getVariables(context).length != other.getVariables(context).length) { return false; }
            for (int i=0; i<this.getVariables(context).length; i++) {
                if ((this.getVariables(context)[i] == null) != (other.getVariables(context)[i] == null)) { return false; }
                if (this.getVariables(context)[i] != null && this.getVariables(context)[i].equals(other.getVariables(context)[i]) == false) { return false; }
            }
        }

        if ((this.getValues(context) != null) && (other.getValues(context) != null)) {
            if (this.getValues(context).length != other.getValues(context).length) { return false; }
            for (int i=0; i<this.getValues(context).length; i++) {
                if ((this.getValues(context)[i] == null) != (other.getValues(context)[i] == null)) { return false; }
                if (this.getValues(context)[i] != null && this.getValues(context)[i].equals(other.getValues(context)[i]) == false) { return false; }
            }
        }

        if ((this.getSubStateVariables(context) != null) && (other.getSubStateVariables(context) != null)) {
            if (this.getSubStateVariables(context).length != other.getSubStateVariables(context).length) { return false; }
            for (int i=0; i<this.getSubStateVariables(context).length; i++) {
                if ((this.getSubStateVariables(context)[i] == null) != (other.getSubStateVariables(context)[i] == null)) { return false; }
                if (this.getSubStateVariables(context)[i] != null && this.getSubStateVariables(context)[i].equals(other.getSubStateVariables(context)[i]) == false) { return false; }
            }
        }

        if ((this.getSubStates(context) != null) && (other.getSubStates(context) != null)) {
            if (this.getSubStates(context).length != other.getSubStates(context).length) { return false; }
            for (int i=0; i<this.getSubStates(context).length; i++) {
                if ((this.getSubStates(context)[i] == null) != (other.getSubStates(context)[i] == null)) { return false; }
                if (this.getSubStates(context)[i] != null && this.getSubStates(context)[i].equals(context, other.getSubStates(context)[i]) == false) { return false; }
            }
        }

        return true;
    }
    
    public Object saveToPersistentType(CallContext context) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        if (this.variables != null) {
            for (int i=0; i<this.variables.length; i++) {
                if (first == false) { sb.append(","); }
                first = false;
                sb.append(this.values[i]);
            }
        }
        if (this.sub_state_variables != null && this.sub_state_variables.length != 0) {
            CustomaryContext.create((Context)context).throwLimitation(context, "Cannot persist complex state with sub state delegates");
            throw (ExceptionLimitation) null; // compilernsists
        }
        return sb.toString();
    }

    public StateComplex loadFromPersistentType(CallContext context, Object persistent_type) {
        if (persistent_type == null) { return this; }
        String[] values = ((String) persistent_type).split(",");
        if (values != null) {
            for (int i=0; i<values.length; i++) {
                this.setValue(context, this.variables[i], values[i], i);
            }
        }
        return this;
    }

    protected Map<String,Vector<UIEquipment>> ui_equipments_map;
    protected String[] visible_state_variables;

    public void loadUIEquipmentsMap(CallContext context, String ui_equipments_map, String... visible_state_variables) {
        this.ui_equipments_map = (Map<String,Vector<UIEquipment>>) com.sphenon.engines.aggregator.Aggregator.create(context, ui_equipments_map);
        this.visible_state_variables = visible_state_variables;
    }

    public Vector<UIEquipment> getUIEquipments(CallContext context) {
        String vsv = this.visible_state_variables == null || this.visible_state_variables.length == 0 ? "main" : this.visible_state_variables[0];
        String main = this.tryGetValue(context, vsv);
        return ui_equipments_map == null ? null : ui_equipments_map.get(main);
    }
}
