import java.io.*;

public class Attack {
    private static final String FILE_NAME = "employee.ser";

    public static void main(String[] args) {
        Employee employee = new Employee("John Doe", "john.doe@company.com", "123456");

        serialize(employee);

        Employee deserializedEmployee = deserialize();
        System.out.println(deserializedEmployee.toString());
    }

    private static void serialize(Employee employee) {
        try (FileOutputStream fileOut = new FileOutputStream(FILE_NAME);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(employee);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Employee deserialize() {
        Employee employee = null;

        try (FileInputStream fileIn = new FileInputStream(FILE_NAME);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            employee = (Employee) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return employee;
    }

    public static class Employee implements Serializable {
        private String name;
        private String email;
        private String password;

        public Employee(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }
}
