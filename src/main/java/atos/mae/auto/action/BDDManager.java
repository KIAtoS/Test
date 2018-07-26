package atos.mae.auto.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import atos.mae.auto.utils.enums.DatabaseDriverEnum;

/**
 * This class manage all bdd connexion, request, ...
 */
public class BDDManager {

	/**
	 * Logger.
	 */
	private static Logger Log = Logger.getLogger(BDDManager.class);

	/**
	 * Sql select request.
	 * @param Driver database driver
	 * @param Host ip or dns where database is located
	 * @param Port port to connect with database
	 * @param User user to log at database
	 * @param Password password to log at database
	 * @param DatabaseName Database's name you need to interact with
	 * @param Request Sql request
	 * @return ResultSet of sql request. Can be null
	 * @throws SQLException something wrong append during connexion or request
	 */
	public static ResultSet select(DatabaseDriverEnum Driver,String Host, String Port, String User, String Password , String DatabaseName ,String Request) throws ClassNotFoundException, SQLException{
		Connection c = null;

		switch(Driver){
			case POSTGRES:
				Class.forName("org.postgresql.Driver");
				c = DriverManager.getConnection("jdbc:postgresql://" + Host + ":" + Port + "/" + DatabaseName,User,Password);
				break;
		}

		Log.info("Opened database successfully");

		final Statement stmt = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		final ResultSet rs = stmt.executeQuery( Request );

		stmt.close();
		c.close();
		return rs;
	}

	/**
	 * Transform String to DatabaseDriverEnum.
	 * @param driver Database driver name as string
	 * @return Database driver name as enum
	 */
	public static DatabaseDriverEnum getDatabaseDriverEnum(String driver){

		if(driver.toUpperCase(Locale.getDefault()).compareTo(DatabaseDriverEnum.POSTGRES.toString()) == 0)
			return DatabaseDriverEnum.POSTGRES;
		else
			return null;
	}
}
