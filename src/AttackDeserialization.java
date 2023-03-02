
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;


public class AttackDeserialization {
    private static final String FILE_NAME = "employee.ser";
    private static final String PUBLIC_KEY = "public-attack.pem";
    private static final String PRIVATE_KEY = "private-attack.pem";
    public static void main(String[] args) {
        KeyPair keyPair = generateKeyPair();

        try {
            savePrivateKey(keyPair.getPrivate(), PRIVATE_KEY);
            savePublicKey(keyPair.getPublic(), PUBLIC_KEY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Employee employee = new Employee("Jane Doe", "jane.doe@company.com", "password2");

        try (FileOutputStream fileOut = new FileOutputStream(FILE_NAME);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            // Tạo một đối tượng Signature sử dụng khóa riêng
            Signature signature = Signature.getInstance("SHA256withRSA");
            PrivateKey privateKey = getPrivateKey();
            signature.initSign(privateKey);

            // Ghi dữ liệu của đối tượng vào ByteArrayOutputStream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(employee);
            oos.flush();
            byte[] data = bos.toByteArray();

            // Ký dữ liệu và lưu chữ ký số cùng với đối tượng đã được serialize
            signature.update(data);
            byte[] signatureBytes = signature.sign();
            out.writeObject(data);
            out.writeObject(signatureBytes);

            System.out.println("Attack successful");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException |
                 InvalidKeySpecException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static PrivateKey getPrivateKey() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
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

        public void setName(String name) {
            this.name = name;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setPassword(String password) {
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

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void savePrivateKey(PrivateKey privateKey, String fileName) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            outputStream.write(privateKey.getEncoded());
        }
    }

    private static void savePublicKey(PublicKey publicKey, String fileName) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            outputStream.write(publicKey.getEncoded());
        }
    }
}

