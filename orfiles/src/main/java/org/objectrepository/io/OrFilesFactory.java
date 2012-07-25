package org.objectrepository.io;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import org.apache.log4j.Logger;
import org.objectrepository.dao.MongoDBSingleton;
import org.objectrepository.exceptions.OrFilesException;
import org.objectrepository.util.Checksum;
import org.objectrepository.utils.Invocations;

import java.io.File;

/**
 * OrFilesFactory
 * <p/>
 * FSGrid implementation for uploading and downloading files.
 * Class is a factory and domain combo.
 * <p/>
 * b = bucket name
 * c = collection
 * d = database
 * h = host
 * l = filename
 * m = md5 checksum
 * p = identifier
 * q = query
 * t = contentType
 */
public abstract class OrFilesFactory implements OrFiles {

    private static String collection = "files";

    private Mongo mongo;
    private String b;
    private String c;
    private String d;
    private String h;
    private String l;
    private String m;
    private String q;
    private String t;
    private String a;
    private String s;

    public Mongo getMongo() {
        return mongo;
    }

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public void setMongo(String[] hosts) {
        mongo = MongoDBSingleton.newInstance(hosts);
        mongo.setWriteConcern(WriteConcern.SAFE);
    }

    public DBCollection getCollection() {
        return getDatabase().getCollection(getC());
    }

    public DB getDatabase() {
        return mongo.getDB(getD());
    }

    public DBCollection getBucket() {
        return mongo.getDB(getD()).getCollection(getB() + ".files"); // Mongo convention: .files and .chunks
    }

    private GridFS gridFS = null;

    public GridFS getGridFS() {
        if (gridFS == null) gridFS = new GridFS(mongo.getDB(getD()), getB());
        return gridFS;
    }

    /**
     * getAlias
     * <p/>
     * Takes the location value from the metadata. If found, returns the stagingfile name.
     *
     * @return
     */
    public void setA(String a) {
        this.a = a;
    }

    public String getA() {
        return (a == null) ? getL() : this.a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getC() {
        return (c == null) ? collection : c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getD() {
        return (d == null) ? "test" : d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public String getL() {
        return l;
    }

    public void setL(String l) {
        this.l = l;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getM() {
        return m;
    }

    public void setM(String m) {
        this.m = m;
    }

    public String getNa() {
        return null; //return m.getNa();
    }

    public String getNamespace() {
        return collection;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getS() {
        if (s == null || s.startsWith("{")) s = "";
        return s;
    }

    public String getPid() {
        return a;
    }

    public String getContentType() {
        return null;
    }

    public String getAccess() {
        return null;
    }

    public String getLabel() {
        return null;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    /**
     * Returns the md5 from the supplied metadata
     * If not available, calculated the md5
     * <p/>
     * pre-pends any md5 with trailing zero's as this is how it is stored in the collection
     *
     * @return
     */
    public String getMd5() {
        if (m == null || m.isEmpty()) {  // fail back...
            final File localFile = new File(getL());
            log.warn("Need to fallback on calculating an md5 checksum as the value is not passed from the command line .");
            m = Checksum.getMD5(localFile);
        }

        return Checksum.getMD5as32Characters(m);
    }

    public void action() throws OrFilesException {
    }

    /**
     * Closes the database connection
     */
    public void close() {
        getGridFS().getDB().getMongo().close();
    }

    public static OrFiles newInstance(String method) throws OrFilesException {
        try {
            final String className = OrFiles.class.getPackage().getName() + ".Or" + Invocations.camelCase(method);
            final Class<OrFiles> c = (Class<OrFiles>) Class.forName(className);
            return c.newInstance();
        } catch (Exception e) {
            throw new OrFilesException(e);
        }
    }

    private static Logger log = Logger.getLogger(OrFilesFactory.class);
}
