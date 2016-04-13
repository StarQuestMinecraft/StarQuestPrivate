package net.countercraft.movecraft.crafttransfer.database;

import java.sql.Connection;

public interface ConnectionProvider {
	public Connection getConnection();
}
