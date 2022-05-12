package leonsch

import java.lang.reflect.Constructor
import java.lang.reflect.Field

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
private fun eqArg(a1: Class<*>, a2: Class<*>): Boolean =
	if(a1.isArray) {
		a2.isArray && eqArg(a1.componentType, a2.componentType)
	} else {
		a1 == a2
	}

private fun eqArgs(a1: Array<out Class<*>>, a2: Array<out Class<*>>) =
	a1.size == a2.size && a1.zip(a2).all { (c1, c2) -> eqArg(c1, c2) }

private fun typeName(type: Class<*>): String =
	type.name

private fun typeNames(args: Array<out Class<*>>): String =
	args.joinToString { typeName(it) }

@Suppress("UNCHECKED_CAST")
fun <T> Class<T>.constructor(vararg args: Class<*>): Constructor<T> =
	(declaredConstructors.find {
		eqArgs(it.parameterTypes, args)
	} ?: throw NoSuchMethodError(
		"Constructor not found (types: ${typeNames(args)}"
	)).apply { isAccessible = true } as Constructor<T>

fun <T> Class<T>.method(name: String, ret: Class<*> = Void.TYPE, vararg args: Class<*>) =
	(declaredMethods.find {
		it.name == name && it.returnType == ret && eqArgs(it.parameterTypes, args)
	} ?: throw NoSuchMethodError(
		"Method not found: ${ret.name} $name(${typeNames(args)})"
	)).apply { isAccessible = true }

fun <T> Class<T>.field(name: String, type: Class<*>): Field =
	(declaredFields.find {
		it.name == name && it.type == type
	} ?: throw NoSuchFieldError(
		"Field not found: ${typeName(type)} $name"
	)).apply { isAccessible = true }
