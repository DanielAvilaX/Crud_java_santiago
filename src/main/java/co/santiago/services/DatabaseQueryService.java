package co.santiago.services;

import java.util.List;
import java.util.Map;

public interface DatabaseQueryService {

    List<Map<String, Object>> executeSelect(String sql);
}