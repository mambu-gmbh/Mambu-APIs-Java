/**
 * 
 */
package com.mambu.apisdk.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation class for defining the password in order to connect to the server
 * 
 * @author ipenciuc
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@BindingAnnotation
public @interface Password {

}
