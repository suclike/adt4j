/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.sviperll.codemodel;

import com.github.sviperll.codemodel.render.Renderable;
import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.render.RendererContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class Type implements Renderable {
    private static final VoidType VOID = new VoidType();
    private static final Type BYTE = Type.primitive(PrimitiveTypeDetails.BYTE);
    private static final Type SHORT = Type.primitive(PrimitiveTypeDetails.SHORT);
    private static final Type INT = Type.primitive(PrimitiveTypeDetails.INT);
    private static final Type LONG = Type.primitive(PrimitiveTypeDetails.LONG);
    private static final Type FLOAT = Type.primitive(PrimitiveTypeDetails.FLOAT);
    private static final Type DOUBLE = Type.primitive(PrimitiveTypeDetails.DOUBLE);
    private static final Type CHAR = Type.primitive(PrimitiveTypeDetails.CHAR);

    public static Type variable(String name) {
        return variable(new TypeVariableDetails(name));
    }

    static Type variable(TypeVariableDetails details) {
        return new TypeVariable(details);
    }

    public static Type voidType() {
        return VOID;
    }

    public static Type intersection(Collection<Type> bounds) throws CodeModelException {
        return intersection(new IntersectionTypeDetails(bounds));
    }

    static Type intersection(IntersectionTypeDetails details) {
        return new IntersectionType(details);
    }

    static Type createObjectType(ObjectTypeDetails typeDetails) {
        return new ObjectType(typeDetails);
    }

    static Type byteType() {
        return BYTE;
    }

    static Type shortType() {
        return SHORT;
    }

    static Type intType() {
        return INT;
    }

    static Type longType() {
        return LONG;
    }

    static Type floatType() {
        return FLOAT;
    }

    static Type doubleType() {
        return DOUBLE;
    }

    static Type charType() {
        return CHAR;
    }

    private static Type primitive(final PrimitiveTypeDetails details) {
        return new PrimitiveType(details);
    }

    private Type() {
    }

    public abstract Kind kind();

    public abstract boolean isVoid();
    public abstract boolean isObjectType();
    public abstract boolean isPrimitive();
    public abstract boolean isArray();
    public abstract boolean isTypeVariable();
    public abstract boolean isWildcard();
    public abstract boolean isIntersection();

    public abstract ObjectTypeDetails getObjectDetails();
    public abstract WildcardTypeDetails getWildcardDetails();
    public abstract PrimitiveTypeDetails getPrimitiveDetails();
    public abstract ArrayTypeDetails getArrayDetails();
    public abstract TypeVariableDetails getVariableDetails();
    public abstract IntersectionTypeDetails getIntersectionDetails();

    public boolean containsWildcards() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Collection<Type> asListOfIntersectedTypes() {
        return isIntersection() ? getIntersectionDetails().intersectedTypes() : Collections.singletonList(this);
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                if (isArray()) {
                    context.appendRenderable(getArrayDetails().elementType());
                    context.appendText("[]");
                } else if (isIntersection()) {
                    Iterator<Type> iterator = getIntersectionDetails().intersectedTypes().iterator();
                    if (iterator.hasNext()) {
                        context.appendRenderable(iterator.next());
                        while (iterator.hasNext()) {
                            context.appendText(" & ");
                            context.appendRenderable(iterator.next());
                        }
                    }
                } else if (isVoid()) {
                    context.appendText("void");
                } else if (isPrimitive()) {
                    context.appendText(getPrimitiveDetails().name().toLowerCase(Locale.US));
                } else if (isTypeVariable()) {
                    context.appendText(getVariableDetails().name());
                } else if (isWildcard()) {
                    WildcardTypeDetails wildcard = getWildcardDetails();
                    context.appendText("?");
                    context.appendWhiteSpace();
                    context.appendText(wildcard.boundKind().name().toLowerCase(Locale.US));
                    context.appendWhiteSpace();
                    context.appendRenderable(wildcard.bound());
                } else if (isObjectType()) {
                    ObjectTypeDetails objectType = getObjectDetails();
                    if (objectType.isRaw())
                        context.appendQualifiedClassName(objectType.definition().qualifiedName());
                    else {
                        context.appendRenderable(objectType.erasure());
                        Iterator<Type> iterator = objectType.typeArguments().iterator();
                        if (iterator.hasNext()) {
                            context.appendText("<");
                            context.appendRenderable(iterator.next());
                            while (iterator.hasNext()) {
                                context.appendText(", ");
                                context.appendRenderable(iterator.next());
                            }
                            context.appendText(">");
                        }
                    }
                }
            }
        };
    }

    public enum Kind {
        VOID, OBJECT, PRIMITIVE, ARRAY, TYPE_VARIABLE, WILDCARD, INTERSECTION
    }

    private static class VoidType extends Type {

        @Override
        public Kind kind() {
            return Kind.VOID;
        }

        @Override
        public boolean isVoid() {
            return true;
        }

        @Override
        public boolean isObjectType() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isTypeVariable() {
            return false;
        }

        @Override
        public boolean isWildcard() {
            return false;
        }

        @Override
        public boolean isIntersection() {
            return false;
        }

        @Override
        public ObjectTypeDetails getObjectDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public WildcardTypeDetails getWildcardDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrimitiveTypeDetails getPrimitiveDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ArrayTypeDetails getArrayDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TypeVariableDetails getVariableDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IntersectionTypeDetails getIntersectionDetails() {
            throw new UnsupportedOperationException();
        }

    }
    private static class TypeVariable extends Type {

        private final TypeVariableDetails details;

        public TypeVariable(TypeVariableDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.TYPE_VARIABLE;
        }

        @Override
        public boolean isVoid() {
            return false;
        }

        @Override
        public boolean isObjectType() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isTypeVariable() {
            return true;
        }

        @Override
        public boolean isWildcard() {
            return false;
        }

        @Override
        public boolean isIntersection() {
            return false;
        }

        @Override
        public ObjectTypeDetails getObjectDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public WildcardTypeDetails getWildcardDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrimitiveTypeDetails getPrimitiveDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ArrayTypeDetails getArrayDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TypeVariableDetails getVariableDetails() {
            return details;
        }

        @Override
        public IntersectionTypeDetails getIntersectionDetails() {
            throw new UnsupportedOperationException();
        }
    }
    private static class IntersectionType extends Type {

        private final IntersectionTypeDetails details;

        public IntersectionType(IntersectionTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.INTERSECTION;
        }

        @Override
        public boolean isVoid() {
            return false;
        }

        @Override
        public boolean isObjectType() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isTypeVariable() {
            return false;
        }

        @Override
        public boolean isWildcard() {
            return false;
        }

        @Override
        public boolean isIntersection() {
            return true;
        }

        @Override
        public ObjectTypeDetails getObjectDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public WildcardTypeDetails getWildcardDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrimitiveTypeDetails getPrimitiveDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ArrayTypeDetails getArrayDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TypeVariableDetails getVariableDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IntersectionTypeDetails getIntersectionDetails() {
            return details;
        }
    }

    private static class ObjectType extends Type {

        private final ObjectTypeDetails details;

        public ObjectType(ObjectTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.OBJECT;
        }

        @Override
        public boolean isVoid() {
            return false;
        }

        @Override
        public boolean isObjectType() {
            return true;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isTypeVariable() {
            return false;
        }

        @Override
        public boolean isWildcard() {
            return false;
        }

        @Override
        public boolean isIntersection() {
            return false;
        }

        @Override
        public ObjectTypeDetails getObjectDetails() {
            return details;
        }

        @Override
        public WildcardTypeDetails getWildcardDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrimitiveTypeDetails getPrimitiveDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ArrayTypeDetails getArrayDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TypeVariableDetails getVariableDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IntersectionTypeDetails getIntersectionDetails() {
            throw new UnsupportedOperationException();
        }
    }

    private static class PrimitiveType extends Type {

        private final PrimitiveTypeDetails details;

        public PrimitiveType(PrimitiveTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.PRIMITIVE;
        }

        @Override
        public boolean isVoid() {
            return false;
        }

        @Override
        public boolean isObjectType() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isTypeVariable() {
            return false;
        }

        @Override
        public boolean isWildcard() {
            return false;
        }

        @Override
        public boolean isIntersection() {
            return false;
        }

        @Override
        public ObjectTypeDetails getObjectDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public WildcardTypeDetails getWildcardDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrimitiveTypeDetails getPrimitiveDetails() {
            return details;
        }

        @Override
        public ArrayTypeDetails getArrayDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TypeVariableDetails getVariableDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IntersectionTypeDetails getIntersectionDetails() {
            throw new UnsupportedOperationException();
        }
    }

}
