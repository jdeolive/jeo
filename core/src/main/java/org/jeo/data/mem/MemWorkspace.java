package org.jeo.data.mem;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.data.Dataset;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;

public class MemWorkspace implements Workspace {

    Map<String,Dataset> data = new LinkedHashMap<String, Dataset>();

    @Override
    public Iterator<String> layers() throws IOException {
        return data.keySet().iterator();
    }

    @Override
    public Dataset get(String layer) throws IOException {
        return data.get(layer);
    }

    public void put(String layer, Dataset dataset) {
        data.put(layer, dataset);
    }

    @Override
    public MemVector create(Schema schema) throws IOException, UnsupportedOperationException {
        MemVector v = new MemVector(schema);
        data.put(schema.getName(), v);
        return v;
    }

    @Override
    public void dispose() {
        data.clear();
    }
}
