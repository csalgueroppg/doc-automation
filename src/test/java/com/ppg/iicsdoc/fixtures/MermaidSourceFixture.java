package com.ppg.iicsdoc.fixtures;

public class MermaidSourceFixture {
    public static String validFlowchart() {
        return """
                flowchart TD
                    A[Start] --> B{Decision}
                    B --> |Yes| C[Process]
                    C --> |No| D{End}
                """;
    }

    public static String validSequenceDiagram() {
        return """
                sequenceDiagram
                    participant User
                    participant API

                    User->>API: GET /customers
                    API-->>User: 200 OK
                """;
    }

    public static String validClassDiagram() {
        return """
                classDiagram
                    class Animal {
                        +String name
                        +move()
                    }

                    class Dog {
                        +bark()
                    }

                    Animal <|-- Dog
                    """;
    }

    public static String validStateDiagram() {
        return """
                stateDiagram 
                    [*] --> Idle 
                    Idle --> Processing
                    Processing --> [*]
                """;
    }

    public static String validERDiagram() {
        return """
                erDiagram
                    CUSTOMER ||--o{ ORDER : places 
                    ORDER ||--|{ LINE_ITEM : contains 
                    
                    CUSTOMER {
                        string name 
                        string address
                    }

                    ORDER {
                        int id
                        date orderDate
                    }
                """;
    }

    public static String validGanttDiagram() {
        return """
                gantt
                    title Project Timeline
                    dateFormat YYYY-MM-DD

                    section Planning
                    Spec definition :done, des1, 2025-01-01, 10d
                    Architecture    :done, des2, 2025-01-11, 10d
                    
                    section Development
                    Implementation  :active, dev1, 2025-01-21, 20d
                    Testing         :dev2, after dev1, 10d
                """;
    }
}
