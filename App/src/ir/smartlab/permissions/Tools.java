/*
 * Projet 	: Permission Explorer
 * Auteur 	: Carlo Criniti
 * Date   	: 2011.06.11
 * 
 * Classe Tools
 * Classe contenant des variables et m�thodes accessibles partout
 */
package ir.smartlab.permissions;

import android.content.Context;
import android.content.res.Resources;

public class Tools {
	// Package de l'application
	private final static String packageName = "ir.smartlab.permissions";
	// Base de donn�es de l'application
	public static Database database;
	/*
	 * getStringResourceByName
	 * Permet de r�cup�rer une chaine de caract�res dans les ressources
	 * n'importe ou dans l'application
	 */
	public static String getStringResourceByName(String name, Resources res, Context context)
	{
	  int resId = res.getIdentifier(name, "string", packageName);
	  return context.getString(resId);
	}
}
