# Decision Tree Training
Java/Kotlin implementation of the ORE-based decision tree training protocol.
This implementation was developed using **OpenJDK 11**.
Building and running the program will work with Java 11, but newer versions of Java might work as well.

## Updatable ORE
The updatable Order Revealing Encryption Scheme is implemented in `src/main/java/decisiontree/ore/updatable`.
It can be used by first specifying a Key-Homomorphic PRF, generating ORE parameters, generating a secret key and then encrypting/comparing:
```java
var khPrf = new NPRPrf<>(NPRPrf.SHA256, Curve25519Group.INSTANCE, Prf.HMAC_SHA256);
var parameters = new UpdatableParams<>(khPrf, 8);
var sk = parameters.generateSecretKey(new SecureRandom());
var ct = sk.encrypt(2);
var m = sk.decrypt(ct);
```
Ciphertexts implement the `Comparable` interface, so `Collections.sort()` or similar will work.

## Decision Tree Training secure protocol
The Decision Tree Training Protocol is implemented in `src/main/java/decisiontree/main/MPCTraining.kt`.

### Running from gradle
To run the protocol with 50 training data points for each party, first run
```shell
./gradlew run --args="alice n=50 dataset=MNIST"
```
After the program has been compiled and is running, run
```shell
./gradlew run --args="bob n=50 dataset=MNIST"
```
for the second party to begin the protocol execution.
The datasets will be downloaded automatically.

### Building
To build the program run
```shell
./gradlew build
```
The built program will be in `build/distributions/`.
To run the built program, untar the resulting tar file and  run 
```shell
bin/DecisionTree alice n=50 dataset=mnist
```
and once the program is running and waiting for a connection, run
```shell
bin/DecisionTree bob n=50 dataset=mnist
```