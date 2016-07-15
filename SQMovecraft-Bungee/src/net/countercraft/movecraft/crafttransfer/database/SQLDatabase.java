package net.countercraft.movecraft.crafttransfer.database;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import net.countercraft.movecraft.crafttransfer.transferdata.TransferData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Exception;

public class SQLDatabase {
	
	private ConnectionProvider con;
	
	public SQLDatabase() {
		con = new BedspawnConnectionProvider();
		createTable(con.getConnection());
	}
	
	private static String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS bungee_craft_transfer (pilot_name varchar(36), transfer_data MEDIUMBLOB, PRIMARY KEY (pilot_name))";
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
		if(!(containsIllegalCharacters(data.getPilot()))) {
			try {
				PreparedStatement s =  getCon().getConnection().prepareStatement(WRITE_DATA_SQL);
				System.out.println("Data: " + data);
				System.out.println("Pilot" + data.getPilot());
				//does some black magic with io streams to make it properly upsert the object as a BLOB
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
				objectStream.writeObject(data);
				byte[] dataBytes = byteStream.toByteArray();
				ByteArrayInputStream inputStream = new ByteArrayInputStream(dataBytes);
				s.setString(1, data.getPilot());
				s.setBlob(2, inputStream);
				s.executeUpdate();
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Wrote TransferData to database for pilot " + data.getPilot());
		}
	}
	//Reads the transferdata from the DB, returns
	public TransferData readData(String pilot) {
		System.out.println("Pilot: " + pilot);
		TransferData data = null;
		if(!(containsIllegalCharacters(pilot))) {
			try {
				PreparedStatement s = getCon().getConnection().prepareStatement(READ_DATA_SQL);
				s.setString(1, pilot);
				ResultSet results = s.executeQuery();
				if(results.next()) {
					System.out.println("Results is not empty");
					//Does some black magic with io streams to make an object
					Blob blob = results.getBlob(1);
					byte[] dataBytes = blob.getBytes(1L, (int) blob.length());
					ByteArrayInputStream byteStream = new ByteArrayInputStream(dataBytes);
					ObjectInputStream objectStream = new ObjectInputStream(byteStream);
					Object dataObject = objectStream.readObject();
					//It's a real object!
					if(dataObject instanceof TransferData) {
						System.out.println("Is TransferData");
						data = (TransferData) dataObject;
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println("Data: " + data);
			System.out.println("Read TransferData object from database for pilot " + data.getPilot());
		}
		return data;
	}
	public boolean containsIllegalCharacters(String input) {
		if((input.contains("'")) || (input.contains("\"")) || (input.contains("\\"))) {
			return true;
		}
		return false;
	}
	//returns the connection provider
	public ConnectionProvider getCon() {
		return con;
	}
}