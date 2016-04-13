package net.countercraft.movecraft.crafttransfer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import net.countercraft.movecraft.crafttransfer.transferdata.TransferData;

import java.lang.Exception;

public class SQLDatabase {
	
	private ConnectionProvider con;
	
	public SQLDatabase() {
		con = new BedspawnConnectionProvider();
		createTable(con.getConnection());
	}
	
	private static String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS bungee_craft_transfer (pilot_name varchar(36), transfer_data BLOB, PRIMARY KEY (pilot_name))";
	private static String WRITE_DATA_SQL = "INSERT INTO bungee_craft_transfer(pilot_name, transfer_data) VALUES (?, ?) ON DUPLICATE KEY UPDATE `pilot_name` = VALUES(pilot_name), `transfer_data` = VALUES(transfer_data)";
	private static String READ_DATA_SQL = "SELECT `transfer_data` FROM bungee_craft_transfer WHERE `pilot_name` = ?";
	
	private void createTable(Connection connection) {
		try {
			Statement s = connection.createStatement();
			s.executeUpdate(CREATE_TABLE_SQL);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	//Writes the transferdata to the DB for reception
	public void writeData(TransferData data) {
		if(!(data.getPilot().contains("\""))) {
			try {
				PreparedStatement s =  getCon().getConnection().prepareStatement(WRITE_DATA_SQL);
				s.setString(1, data.getPilot());
				s.setObject(2, data);
				s.executeUpdate();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Wrote TransferData to database for pilot " + data.getPilot());
		}
	}
	//Reads the transferdata from the DB, returns
	public TransferData readData(String pilot) {
		TransferData data = null;
		if(!pilot.contains("\"")) {
			try {
				PreparedStatement s = getCon().getConnection().prepareStatement(READ_DATA_SQL);
				s.setString(1, pilot);
				ResultSet result = s.executeQuery();
				if((result.next()) && (result.getObject(1) instanceof TransferData)) {
					data = (TransferData) result.getObject(1);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println("Read TransferData object from database for pilot " + data.getPilot());
		}
		return data;
	}
	//returns the connection provider
	public ConnectionProvider getCon() {
		return con;
	}
}
