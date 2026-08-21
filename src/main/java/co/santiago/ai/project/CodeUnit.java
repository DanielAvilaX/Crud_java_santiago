package co.santiago.ai.project;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CodeUnit {

    private String kind;
    private String packageName;
    private String typeName;

    private List<String> enumConstants = new ArrayList<>();
    private List<String> fields = new ArrayList<>();
}
