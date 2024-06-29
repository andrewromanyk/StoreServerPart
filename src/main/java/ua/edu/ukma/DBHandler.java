package ua.edu.ukma;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHandler {
    Session session;
    CriteriaBuilder cb;


    Connection conn;
    private String name = "StoreDB";
    private String createGroup = """
            CREATE TABLE IF NOT EXISTS groups (
                id_group serial PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE,
                description VARCHAR(255) NOT NULL
            );
            """;

    private String createGoods = """
            CREATE TABLE IF NOT EXISTS goods (
                id_good serial PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE,
                description VARCHAR(255) NOT NULL,
                manufacturer VARCHAR(255) NOT NULL,
                amount INTEGER NOT NULL,
                price REAL NOT NULL,
                id_group INTEGER NOT NULL,
                FOREIGN KEY (id_group) REFERENCES groups(id_group)
            );
            """;

    private String createPass = """
            CREATE TABLE IF NOT EXISTS pass (
                id_pass serial PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE,
                hash VARCHAR(255) NOT NULL
            );
            """;

    public static void main(String[] args) throws SQLException, JsonProcessingException {
        DBHandler db =  new DBHandler();
        db.init();
        //System.out.println(db.getAllGroups());
    }

    public static void printResult(ResultSet rs) throws SQLException {
        System.out.println("[");
        while(rs.next()){
            System.out.println("\t{" + rs.getInt("id_good") + ", " + rs.getString("name") + ", "
                    + rs.getString("description") + ", " + rs.getString("manufacturer") + ", "
                    + rs.getInt("amount") + ", " + rs.getDouble("price") + ", "
                    + rs.getInt("id_group")
                    + "}");
        }
        System.out.println("]");
    }

    public static <T> void printList(List<T> list){
        System.out.println("[");
        for (T item : list){
            if (list.indexOf(item) == list.size()-1){
                System.out.println("\t" + item + "\n");
            }
            else System.out.print("\t" + item + ",\n");
        }
        System.out.print("]");
    }

    public void init() {
        if (conn != null) {
            return;
        }
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://ep-withered-water-a2enkxp5.eu-central-1.aws.neon.tech/neondb?user=neondb_owner&password=WeVp1NZbR4jl&sslmode=require");
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_NONE);

        }
        catch (SQLException | ClassNotFoundException e){
            System.err.println("Couldn't connect to database or load JDBC driver");
        }
        try {
            PreparedStatement crtGrp = conn.prepareStatement(createGroup);
            PreparedStatement crtGds = conn.prepareStatement(createGoods);
            PreparedStatement crtPas = conn.prepareStatement(createPass);
            crtGrp.executeUpdate();
            crtGds.executeUpdate();
            crtPas.executeUpdate();
        }
        catch (SQLException e){
            System.err.println("Couldn't create tables.");
        }
        session = HibernateUtil.getHibernateSession();
        cb = session.getCriteriaBuilder();
    }

    public void stopConnection() throws SQLException {
        session.close();
        session = null;
        conn.close();
        conn = null;
        cb = null;
    }

    //Create product
    public int createProduct(String name, String descr, String manuf, int amount, double price, int group) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
                                   INSERT INTO goods VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)
                               """, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.setString(2, descr);
        ps.setString(3, manuf);
        ps.setInt(4, amount);
        ps.setDouble(5, price);
        ps.setInt(6, group);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        int result = rs.getInt(1);
        System.out.println("Result:" + result);
        ps.close();
        return result;
    }

    public int createGroup(String name, String descr) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
                                   INSERT INTO groups VALUES (DEFAULT, ?, ?)
                               """, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.setString(2, descr);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        int result = rs.getInt(1);
        System.out.println("Result:" + result);
        ps.close();
        return result;
    }

    //Get Product
    public ResultSet getAllProducts() throws SQLException {
        Statement ps = conn.createStatement();
        return ps.executeQuery("SELECT * FROM goods");
    }

    public List<groups> getAllGroups(){
        CriteriaQuery<groups> cr = cb.createQuery(groups.class);
        Root<groups> root = cr.from(groups.class);
        cr.select(root);
        Query<groups> query = session.createQuery(cr);
        return query.getResultList();
    }

    public List<String> getCreds(String login) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
                                                        SELECT name, hash
                                                        FROM pass
                                                        WHERE name=?""");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) return null;
        List<String> lst = new ArrayList<>();
        lst.add(rs.getString(1));
        lst.add(rs.getString(2));
        System.out.println(lst.get(0) + " + " + lst.get(1));
        return lst;
    }

    public ResultSet getProduct(String name) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
                                                        SELECT *
                                                        FROM goods
                                                        WHERE name=?""");
        ps.setString(1, name);
        return ps.executeQuery();
    }

    public ResultSet getProduct(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
                                                        SELECT *
                                                        FROM goods
                                                        WHERE id_good=?""");
        ps.setInt(1, id);
        return ps.executeQuery();
    }

    private void CriteriaUpdateSetterGood(String[] args, CriteriaUpdate<goods> criteriaUpdate) {
        int length = args.length;

        if (length > 0 && args[0] != null) criteriaUpdate.set("name", args[0]);
        if (length > 1 && args[1] != null) criteriaUpdate.set("description", args[1]);
        if (length > 2 && args[2] != null) criteriaUpdate.set("manufacturer", args[2]);
        if (length > 3 && args[3] != null) criteriaUpdate.set("amount", Integer.parseInt(args[3]));
        if (length > 4 && args[4] != null) criteriaUpdate.set("price", Double.parseDouble(args[4]));
        if (length > 5 && args[5] != null) criteriaUpdate.set("id_group", Integer.parseInt(args[5]));
    }

    //Update product
    private <T> int updateByHelperGood(String[] args, String by, T value){
        CriteriaUpdate<goods> criteriaUpdate = cb.createCriteriaUpdate(goods.class);
        Root<goods> root = criteriaUpdate.from(goods.class);
        CriteriaUpdateSetterGood(args, criteriaUpdate);
        criteriaUpdate.where(cb.equal(root.get(by), value));

//        if (length > 0 && args[0] != null) criteriaUpdate.set("name", args[0]);
//        if (length > 1 && args[1] != null) criteriaUpdate.set("description", args[1]);
//        if (length > 2 && args[2] != null) criteriaUpdate.set("manufacturer", args[2]);
//        if (length > 3 && args[3] != null) criteriaUpdate.set("amount", Integer.parseInt(args[3]));
//        if (length > 4 && args[4] != null) criteriaUpdate.set("price", Double.parseDouble(args[4]));
//        if (length > 5 && args[5] != null) criteriaUpdate.set("id_group", Integer.parseInt(args[5]));

        Transaction transaction = session.beginTransaction();
        int res = 0;
        try {
            res = session.createQuery(criteriaUpdate).executeUpdate();
        }
        finally {
            transaction.commit();
        }
        return res;
    }

    public int updateByName(String name, String[] args){
        return updateByHelperGood(args, "name", name);
    }

    public int updateByIdGood(int id, String[] args){
        return updateByHelperGood(args, "id_good", id);
    }

    public int updateByGroup(int id, String[] args){
        return updateByHelperGood(args, "id_group", id);
    }

    public int updateByManufacturer(String manuf, String[] args){
        return updateByHelperGood(args, "manufacturer", manuf);
    }

    private void CriteriaUpdateSetterGroup(String[] args, CriteriaUpdate<groups> criteriaUpdate) {
        int length = args.length;

        if (length > 0 && args[0] != null) criteriaUpdate.set("name", args[0]);
        if (length > 1 && args[1] != null) criteriaUpdate.set("description", args[1]);
    }

    //Update product
    private <T> int updateByHelperGroup(String[] args, String by, T value){
        CriteriaUpdate<groups> criteriaUpdate = cb.createCriteriaUpdate(groups.class);
        Root<groups> root = criteriaUpdate.from(groups.class);
        CriteriaUpdateSetterGroup(args, criteriaUpdate);
        criteriaUpdate.where(cb.equal(root.get(by), value));

//        if (length > 0 && args[0] != null) criteriaUpdate.set("name", args[0]);
//        if (length > 1 && args[1] != null) criteriaUpdate.set("description", args[1]);
//        if (length > 2 && args[2] != null) criteriaUpdate.set("manufacturer", args[2]);
//        if (length > 3 && args[3] != null) criteriaUpdate.set("amount", Integer.parseInt(args[3]));
//        if (length > 4 && args[4] != null) criteriaUpdate.set("price", Double.parseDouble(args[4]));
//        if (length > 5 && args[5] != null) criteriaUpdate.set("id_group", Integer.parseInt(args[5]));

        Transaction transaction = session.beginTransaction();
        int res = 0;
        try {
            res = session.createQuery(criteriaUpdate).executeUpdate();
        }
        finally {
            transaction.commit();
        }
        return res;
    }

    public int updateByIdGroup(int id, String[] args){
        return updateByHelperGroup(args, "id_group", id);
    }

    //Delete product
    public <T> int deleteByGood(String by, T value){
        CriteriaDelete<goods> criteriaDelete = cb.createCriteriaDelete(goods.class);
        Root<goods> root = criteriaDelete.from(goods.class);
        criteriaDelete.where(cb.equal(root.get(by), value));

        Transaction transaction = session.beginTransaction();
        int res = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        return res;
    }

    public <T> int deleteByGroup(String by, T value){
        CriteriaDelete<groups> criteriaDelete = cb.createCriteriaDelete(groups.class);
        Root<groups> root = criteriaDelete.from(groups.class);
        criteriaDelete.where(cb.equal(root.get(by), value));

        Transaction transaction = session.beginTransaction();
        int res = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        return res;
    }

    public <T, K> int deleteByTwoGood(String by, T value, String by2, K value2){
        CriteriaDelete<goods> criteriaDelete = cb.createCriteriaDelete(goods.class);
        Root<goods> root = criteriaDelete.from(goods.class);
        criteriaDelete.where(cb.and(cb.equal(root.get(by), value), cb.equal(root.get(by2), value2)));

        Transaction transaction = session.beginTransaction();
        int res = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        return res;
    }

    public int deleteByIdGood(int id){
        return deleteByGood("id_good", id);
    }
    public int deleteByGroupGood(int id){
        return deleteByGood("id_group", id);
    }
    public int deleteByManufacturerGood(String manuf){
        return deleteByGood("manufacturer", manuf);
    }
    public int deleteByNameGood(String name){
        return deleteByGood("name", name);
    }
    public int deleteByManufacturerAndGroupGood(String manuf, String group){
        return deleteByTwoGood("manufacturer", manuf, "id_group", group);
    }

    public int deleteByIdGroup(int id){
        return deleteByGroup("id_group", id);
    }

    //List by criteria
    //Products with amount = 0
    public List<goods> getAllProductsList(){
        CriteriaQuery<goods> cr = cb.createQuery(goods.class);
        Root<goods> root = cr.from(goods.class);
        cr.select(root);
        Query<goods> query = session.createQuery(cr);
        return query.getResultList();
    }


    public List<goods> getNonexistentProducts(){
        CriteriaQuery<goods> cr = cb.createQuery(goods.class);
        Root<goods> root = cr.from(goods.class);
        cr.select(root);
        cr.where(cb.equal(root.get("amount"), 0));

        Query<goods> query = session.createQuery(cr);
        return query.getResultList();
    }

    //Sorting products by price
    public List<goods> getProductsByPrice(boolean asc){
        CriteriaQuery<goods> cr = cb.createQuery(goods.class);
        Root<goods> root = cr.from(goods.class);
        cr.select(root);
        if (asc) cr.orderBy(cb.asc(root.get("price")));
        else cr.orderBy(cb.desc(root.get("price")));

        Query<goods> query = session.createQuery(cr);
        return query.getResultList();
    }

    //Get groups of products that have at least 1 souldout product
//    public List<GroupCount> getSoldoutProductsByGroup(){
//        CriteriaQuery<GroupCount> cr = cb.createQuery(GroupCount.class);
//        Root<goods> root = cr.from(goods.class);
//        cr.multiselect(root.get("id_group"), cb.count(root.get("id_group")).alias("count"));
//        cr.where(cb.equal(root.get("amount"), 0));
//        cr.groupBy(root.get("id_group"));
//
//        Query<GroupCount> query = session.createQuery(cr);
//        return query.getResultList();
//    }

}
