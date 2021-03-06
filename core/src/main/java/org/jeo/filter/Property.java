package org.jeo.filter;

import org.jeo.feature.Feature;

/**
 * Evaluates a field/property of a {@link Feature} object.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class Property implements Expression {

    String property;

    public Property(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public Object evaluate(Object obj) {
        if (obj instanceof Feature) {
            return ((Feature)obj).get(property);
        }
        return null;
    }

    @Override
    public Object accept(FilterVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((property == null) ? 0 : property.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Property other = (Property) obj;
        if (property == null) {
            if (other.property != null)
                return false;
        } else if (!property.equals(other.property))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + property + "]";
    }
}
