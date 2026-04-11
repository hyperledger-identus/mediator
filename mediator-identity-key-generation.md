### Step-by-Step Guide to Generate Keys for the Mediator Identity

1. **Install OpenSSL**:
   - **Linux**:
     - If you haven't already, install OpenSSL on your Linux system using your package manager. Here are the commands for various package managers:

       ```bash
       sudo apt-get update && sudo apt-get install openssl
       ```

       ```bash
       sudo yum install openssl  # Red Hat-based systems
       ```

       ```bash
       sudo pacman -S openssl    # Arch Linux
       ```

   - **macOS**:
     - OpenSSL is typically pre-installed on macOS. If it's not available or you need a newer version, you can install it using Homebrew:

       ```bash
       brew install openssl
       ```

2. **Install jq**:
   - **Linux**:
     - If you haven't already, install jq on your Linux system using your package manager. Here are the commands for various package managers:

       ```bash
       sudo apt-get update && sudo apt-get install jq
       ```

       ```bash
       sudo yum install jq       # Red Hat-based systems
       ```

       ```bash
       sudo pacman -S jq         # Arch Linux
       ```

   - **macOS**:
     - If you haven't already, install jq on your macOS system using Homebrew:

       ```bash
       brew install jq
       ```

3. **Generate X25519 Key (for KEY_AGREEMENT)**:
   - Run the following command to generate the X25519 key:

    ```bash
    openssl genpkey -algorithm X25519 -out private_key_x25519.pem
    ```

4. **Format X25519 Key into JWK**:
   - Run the following command to format the X25519 key into JWK format:

     ```bash
     jq -nR --arg d "$(openssl pkey -inform pem -in private_key_x25519.pem -noout -text | awk '/priv:/{flag=1; next} /pub:/{flag=0} flag' | sed 's/[^0-9A-Fa-f]//g' | xxd -r -p | base64 | tr -d '\n' | tr '+/' '-_' | sed 's/=*$//')" --arg x "$(openssl pkey -inform pem -in private_key_x25519.pem -noout -text | awk '/pub:/{flag=1; next} /priv:/{flag=0} flag' | sed 's/[^0-9A-Fa-f]//g' | xxd -r -p | base64 | tr -d '\n' | tr '+/' '-_' | sed 's/=*$//')" '{kty: "OKP", crv: "X25519", x: $x, d: $d}'
     ```

5. **Generate Ed25519 Key (for KEY_AUTHENTICATION)**:
   - Run the following command to generate the Ed25519 key:

     ```bash
     openssl genpkey -algorithm Ed25519 -out private_key_ed25519.pem
     ```

6. **Format Ed25519 Key into JWK**:
   - Run the following command to format the Ed25519 key into JWK format:

     ```bash
     jq -nR --arg d "$(openssl pkey -inform pem -in private_key_ed25519.pem -noout -text | awk '/priv:/{flag=1; next} /pub:/{flag=0} flag' | sed 's/[^0-9A-Fa-f]//g' | xxd -r -p | base64 | tr -d '\n' | tr '+/' '-_' | sed 's/=*$//')" --arg x "$(openssl pkey -inform pem -in private_key_ed25519.pem -noout -text | awk '/pub:/{flag=1; next} /priv:/{flag=0} flag' | sed 's/[^0-9A-Fa-f]//g' | xxd -r -p | base64 | tr -d '\n' | tr '+/' '-_' | sed 's/=*$//')" '{kty: "OKP", crv: "Ed25519", x: $x, d: $d}'
     ```

These commands will guide you to generate X25519 and Ed25519 keys using OpenSSL and format them into JWK format suitable for use as KEY_AGREEMENT and KEY_AUTHENTICATION keys, respectively.
