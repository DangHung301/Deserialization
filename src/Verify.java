import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Verify {
    private static final String FILE_NAME = "employee.ser";
    private static final String PUBLIC_KEY = "public.pem";
    private static final String PRIVATE_KEY = "private.pem";

    public static void main(String[] args) {

        KeyPair keyPair = generateKeyPair();

        try {
            savePrivateKey(keyPair.getPrivate(), PRIVATE_KEY);
            savePublicKey(keyPair.getPublic(), PUBLIC_KEY);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        Employee employee = new Employee("John Doe", "john.doe@company.com", "password");
//
//        serialize(employee);

        Employee deserializedEmployee = deserialize();
        System.out.println(deserializedEmployee.toString());
    }

    private static void serialize(Employee employee) {
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
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException |
                 ClassNotFoundException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private static Employee deserialize() {
        Employee employee = null;

        try (FileInputStream fileIn = new FileInputStream(FILE_NAME);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {

            // Đọc dữ liệu của đối tượng và chữ ký số
            byte[] data = (byte[]) in.readObject();
            byte[] signatureBytes = (byte[]) in.readObject();

            // Tạo một đối tượng Signature sử dụng khóa công khai
            Signature signature = Signature.getInstance("SHA256withRSA");
            PublicKey publicKey = getPublicKey();
            signature.initVerify(publicKey);

            // Xác thực tính hợp lệ của chữ ký số và đối tượng đã được deserialize
            signature.update(data);
            boolean verified = signature.verify(signatureBytes);

            // Nếu chữ ký số hợp lệ thì tiến hành deserialize đối tượng
            if (verified) {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                employee = (Employee) ois.readObject();
            } else {
                System.out.println("Invalid signature");
            }
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException |
                 SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        return employee;

    }

    private static PublicKey getPublicKey() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
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

