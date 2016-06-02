package net.countercraft.movecraft.crafttransfer.database;

import java.sql.Connection;

import net.countercraft.movecraft.bedspawns.Bedspawn;

public class BedspawnConnectionProvider implements ConnectionProvider {
	public Connection getConnection() {
		System.out.println("getContext: " + Bedspawn.getContext());
		return Bedspawn.cntx;
	}
}
