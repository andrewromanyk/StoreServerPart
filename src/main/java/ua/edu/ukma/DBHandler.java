package ua.edu.ukma;

import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.*;
import java.util.List;

public class DBHandler {
    Session session;
    CriteriaBuilder cb;

    private Connection conn;
    private String name = "StoreDB";
    private String createGroup = """
            CREATE TABLE IF NOT EXISTS 'groups' (
                'id_group' INTEGER PRIMARY KEY AUTOINCREMENT,
                'name' VARCHAR(255) NOT NULL,
                'description' VARCHAR(255) NOT NULL
            );
            """;

    private String createGoods = """
            CREATE TABLE IF NOT EXISTS 'goods' (
                'id_good' INTEGER PRIMARY KEY AUTOINCREMENT,
                'name' VARCHAR(255) NOT NULL,
                'description' VARCHAR(255) NOT NULL,
                'manufacturer' VARCHAR(255) NOT NULL,
                'amount' INTEGER NOT NULL,
                'price' REAL NOT NULL,
                'id_group' INTEGER NOT NULL,
                FOREIGN KEY (id_group) REFERENCES groups(id_group)
            );
            """;

    public static void main(String[] args) throws SQLException {
        DBHandler db =  new DBHandler();
        db.init();
        db.updateByManufacturer("unknown", new String[]{null, null, "Chysto", null, null});
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
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + name);
        }
        catch (SQLException | ClassNotFoundException e){
            System.err.println("Couldn't connect to database or load JDBC driver");
        }
        try {
            PreparedStatement crtGrp = conn.prepareStatement(createGroup);
            PreparedStatement crtGds = conn.prepareStatement(createGoods);
            crtGrp.executeUpdate();
            crtGds.executeUpdate();
        }
        catch (SQLException e){
            System.err.println("Couldn't create tables.");
        }
        session = HibernateUtil.getHibernateSession();
        cb = session.getCriteriaBuilder();
    }

    //Create product
    public int createProduct(String name, String descr, String manuf, int amount, double price, int group) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
                                   INSERT INTO goods VALUES (null, ?, ?, ?, ?, ?, ?)
                               """);
        ps.setString(1, name);
        ps.setString(2, descr);
        ps.setString(3, manuf);
        ps.setInt(4, amount);
        ps.setDouble(5, price);
        ps.setInt(6, group);
        ps.executeUpdate();
        int result = ps.getGeneratedKeys().getInt(1);
        ps.close();
        return result;
    }

    //Get Product
    public ResultSet getAllProducts() throws SQLException {
        Statement ps = conn.createStatement();
        return ps.executeQuery("SELECT * FROM goods");
    }

    public ResultSet getCreds(String login) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
                                                        SELECT *
                                                        FROM login_creds
                                                        WHERE login=?""");
        ps.setString(1, name);
        return ps.executeQuery();
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

    private void CriteriaUpdateSetter(String[] args, CriteriaUpdate<goods> criteriaUpdate) {
        int length = args.length;

        if (length > 0 && args[0] != null) criteriaUpdate.set("name", args[0]);
        if (length > 1 && args[1] != null) criteriaUpdate.set("description", args[1]);
        if (length > 2 && args[2] != null) criteriaUpdate.set("manufacturer", args[2]);
        if (length > 3 && args[3] != null) criteriaUpdate.set("amount", Integer.parseInt(args[3]));
        if (length > 4 && args[4] != null) criteriaUpdate.set("price", Double.parseDouble(args[4]));
        if (length > 5 && args[5] != null) criteriaUpdate.set("id_group", Integer.parseInt(args[5]));
    }

    //Update product
    private <T> int updateByHelper(String[] args, String by, T value){
        CriteriaUpdate<goods> criteriaUpdate = cb.createCriteriaUpdate(goods.class);
        Root<goods> root = criteriaUpdate.from(goods.class);
        CriteriaUpdateSetter(args, criteriaUpdate);
        criteriaUpdate.where(cb.equal(root.get(by), value));

//        if (length > 0 && args[0] != null) criteriaUpdate.set("name", args[0]);
//        if (length > 1 && args[1] != null) criteriaUpdate.set("description", args[1]);
//        if (length > 2 && args[2] != null) criteriaUpdate.set("manufacturer", args[2]);
//        if (length > 3 && args[3] != null) criteriaUpdate.set("amount", Integer.parseInt(args[3]));
//        if (length > 4 && args[4] != null) criteriaUpdate.set("price", Double.parseDouble(args[4]));
//        if (length > 5 && args[5] != null) criteriaUpdate.set("id_group", Integer.parseInt(args[5]));

        Transaction transaction = session.beginTransaction();
        int res = session.createQuery(criteriaUpdate).executeUpdate();
        transaction.commit();
        return res;
    }

    public int updateByName(String name, String[] args){
        return updateByHelper(args, "name", name);
    }

    public int updateById(int id, String[] args){
        return updateByHelper(args, "id_good", id);
    }

    public int updateByGroup(int id, String[] args){
        return updateByHelper(args, "id_group", id);
    }

    public int updateByManufacturer(String manuf, String[] args){
        return updateByHelper(args, "manufacturer", manuf);
    }

    //Delete product
    public <T> int deleteBy(String by, T value){
        CriteriaDelete<goods> criteriaDelete = cb.createCriteriaDelete(goods.class);
        Root<goods> root = criteriaDelete.from(goods.class);
        criteriaDelete.where(cb.equal(root.get(by), value));

        Transaction transaction = session.beginTransaction();
        int res = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        return res;
    }
    public <T, K> int deleteByTwo(String by, T value, String by2, K value2){
        CriteriaDelete<goods> criteriaDelete = cb.createCriteriaDelete(goods.class);
        Root<goods> root = criteriaDelete.from(goods.class);
        criteriaDelete.where(cb.and(cb.equal(root.get(by), value), cb.equal(root.get(by2), value2)));

        Transaction transaction = session.beginTransaction();
        int res = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        return res;
    }

    public int deleteById(int id){
        return deleteBy("id_good", id);
    }
    public int deleteByGroup(int id){
        return deleteBy("id_group", id);
    }
    public int deleteByManufacturer(String manuf){
        return deleteBy("manufacturer", manuf);
    }
    public int deleteByName(String name){
        return deleteBy("name", name);
    }

    public int deleteByManufacturerAndGroup(String manuf, String group){
        return deleteByTwo("manufacturer", manuf, "id_group", group);
    }

    //List by criteria
    //Products with amount = 0
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
