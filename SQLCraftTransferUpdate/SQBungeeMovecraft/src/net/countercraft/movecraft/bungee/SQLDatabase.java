package net.countercraft.movecraft.bungee;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;

public class SQLDatabase {
	
	ConnectionProvider con;
	
	private static String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS craft_transfer (pilot varchar(32), data BLOB, primary key (pilot))";
	
	private static String UPSERT_SQL = "INSERT INTO craft_transfer VALUES(?, ?) ON DUPLICATE KEY UPDATE `pilot` = VALUES(pilot), `data` = VALUES(data)";
	
	private static String READ_SQL = "SELECT `data` FROM craft_transfer WHERE `pilot` = ?";
	
	public SQLDatabase() {
		con = new BedspawnConnectionProvider();
		createTable();
	}
	//creates table
	private void createTable() {
		try {
			Statement s = getCon().getConnection().createStatement();
			s.executeUpdate(CREATE_TABLE_SQL);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//Upserts data to db
	public void writeData(TransferData data) {
		try {
			PreparedStatement s = getCon().getConnection().prepareStatement(UPSERT_SQL);
			s.setString(1, data.getPilotName());
			s.setObject(2, data);
			s.executeUpdate();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//Returns TransferData obj from query ResultSet
	public TransferData readData(String pilotName) {
		if(!(containsIllegalCharacters(pilotName))) {
			try {
				PreparedStatement s = getCon().getConnection().prepareStatement(READ_SQL);
				s.setString(1, pilotName);
				ResultSet resultSet = s.executeQuery();
				if(resultSet.next()) {
					byte[] b = resultSet.getBytes(1);
					ObjectInputStream objectStream = null;
					if(b != null) {
						objectStream = new ObjectInputStream(new ByteArrayInputStream(b));
						Object deserializedObject = objectStream.readObject();
						return (TransferData) deserializedObject;
					}
				}
			}
			catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
		return null;
	}
	private ConnectionProvider getCon() {
		return con;
	}
	//Checks that nobody is trying to perform an injection attack
	private boolean containsIllegalCharacters(String input) {
		if((input.contains("\"")) || (input.contains("\\"))) {
			return true;
		}
		return false;
	}
}
