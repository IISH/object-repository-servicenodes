package com.mongodb.gridfs;

/**
 * ReplicaGridFSDBFile
 *
 * Enables the setting of the GridFS
 */
public class ReplicaGridFSDBFile extends GridFSDBFile {

    public ReplicaGridFSDBFile() {
        super();
    }

    public void setGridFS(GridFS fs) {
        this._fs = fs;
    }
}
