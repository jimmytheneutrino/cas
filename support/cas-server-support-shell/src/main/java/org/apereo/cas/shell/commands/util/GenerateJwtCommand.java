package org.apereo.cas.shell.commands.util;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.DirectDecrypter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link GenerateJwtCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("Utilities")
@ShellComponent
@Slf4j
public class GenerateJwtCommand {

    private static final int SEP_LENGTH = 8;

    private static final int DEFAULT_SIGNING_SECRET_SIZE = 256;
    private static final int DEFAULT_ENCRYPTION_SECRET_SIZE = 48;
    private static final String DEFAULT_SIGNING_ALGORITHM = "HS256";
    private static final String DEFAULT_ENCRYPTION_ALGORITHM = "dir";
    private static final String DEFAULT_ENCRYPTION_METHOD = "A192CBC-HS384";

    /**
     * Generate.
     *
     * @param subject the subject
     */
    public void generate(final String subject) {
        generate(DEFAULT_SIGNING_SECRET_SIZE, DEFAULT_ENCRYPTION_SECRET_SIZE,
            DEFAULT_SIGNING_ALGORITHM, DEFAULT_ENCRYPTION_ALGORITHM,
            DEFAULT_ENCRYPTION_METHOD, subject);
    }

    /**
     * Generate.
     *
     * @param signingSecretSize    the signing secret size
     * @param encryptionSecretSize the encryption secret size
     * @param signingAlgorithm     the signing algorithm
     * @param encryptionAlgorithm  the encryption algorithm
     * @param encryptionMethod     the encryption algorithm
     * @param subject              the subject
     */
    @ShellMethod(key = "generate-jwt", value = "Generate a JWT with given size and algorithm for signing and encryption.")
    public void generate(
        @ShellOption(value = {"signingSecretSize"},
            help = "Size of the signing secret",
            defaultValue = "" + DEFAULT_SIGNING_SECRET_SIZE) final int signingSecretSize,
        @ShellOption(value = {"encryptionSecretSize"},
            help = "Size of the encryption secret",
            defaultValue = "" + DEFAULT_ENCRYPTION_SECRET_SIZE) final int encryptionSecretSize,
        @ShellOption(value = {"signingAlgorithm"},
            help = "Algorithm to use for signing",
            defaultValue = DEFAULT_SIGNING_ALGORITHM) final String signingAlgorithm,
        @ShellOption(value = {"encryptionAlgorithm"},
            help = "Algorithm to use for encryption",
            defaultValue = DEFAULT_ENCRYPTION_ALGORITHM) final String encryptionAlgorithm,
        @ShellOption(value = {"encryptionMethod"},
            help = "Method to use for encryption",
            defaultValue = DEFAULT_ENCRYPTION_METHOD) final String encryptionMethod,
        @ShellOption(value = {"subject"},
            help = "Subject to use for the JWT") final String subject) {

        final JwtGenerator<CommonProfile> g = new JwtGenerator<>();

        configureJwtSigning(signingSecretSize, signingAlgorithm, g);
        configureJwtEncryption(encryptionSecretSize, encryptionAlgorithm, encryptionMethod, g);

        final var profile = new CommonProfile();
        profile.setId(subject);

        final var repeat = StringUtils.repeat('=', SEP_LENGTH);
        LOGGER.debug(repeat);
        LOGGER.info("\nGenerating JWT for subject [{}] with signing key size [{}], signing algorithm [{}], "
                + "encryption key size [{}], encryption method [{}] and encryption algorithm [{}]\n",
            subject, signingSecretSize, signingAlgorithm, encryptionSecretSize, encryptionMethod, encryptionAlgorithm);
        LOGGER.debug(repeat);

        final var token = g.generate(profile);
        LOGGER.info("==== JWT ====\n[{}]", token);
    }

    private void configureJwtEncryption(final int encryptionSecretSize, final String encryptionAlgorithm,
                                        final String encryptionMethod, final JwtGenerator<CommonProfile> g) {
        if (encryptionSecretSize <= 0 || StringUtils.isBlank(encryptionMethod) || StringUtils.isBlank(encryptionAlgorithm)) {
            LOGGER.info("No encryption algorithm or size specified, so the generated JWT will not be encrypted");
            return;
        }

        final var encryptionSecret = RandomStringUtils.randomAlphanumeric(encryptionSecretSize);
        LOGGER.info("==== Encryption Secret ====\n[{}]\n", encryptionSecret);

        final var acceptedEncAlgs = Arrays.stream(JWEAlgorithm.class.getDeclaredFields())
            .filter(f -> f.getType().equals(JWEAlgorithm.class))
            .map(Unchecked.function(f -> {
                f.setAccessible(true);
                return ((JWEAlgorithm) f.get(null)).getName();
            }))
            .collect(Collectors.joining(","));
        LOGGER.debug("Encryption algorithm: [{}]. Available algorithms are [{}]", encryptionAlgorithm, acceptedEncAlgs);

        final var acceptedEncMethods = Arrays.stream(EncryptionMethod.class.getDeclaredFields())
            .filter(f -> f.getType().equals(EncryptionMethod.class))
            .map(Unchecked.function(f -> {
                f.setAccessible(true);
                return ((EncryptionMethod) f.get(null)).getName();
            }))
            .collect(Collectors.joining(","));
        LOGGER.debug("Encryption method: [{}]. Available methods are [{}]", encryptionMethod, acceptedEncMethods);

        final var algorithm = JWEAlgorithm.parse(encryptionAlgorithm);
        final var encryptionMethodAlg = EncryptionMethod.parse(encryptionMethod);

        if (DirectDecrypter.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            if (!DirectDecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(encryptionMethodAlg)) {
                LOGGER.warn("Encrypted method [{}] is not supported for algorithm [{}]. Accepted methods are [{}]",
                    encryptionMethod, encryptionAlgorithm, DirectDecrypter.SUPPORTED_ENCRYPTION_METHODS);
                return;
            }
        }
        if (AESDecrypter.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            if (!AESDecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(encryptionMethodAlg)) {
                LOGGER.warn("Encrypted method [{}] is not supported for algorithm [{}]. Accepted methods are [{}]",
                    encryptionMethod, encryptionAlgorithm, AESDecrypter.SUPPORTED_ENCRYPTION_METHODS);
                return;
            }
        }

        g.setEncryptionConfiguration(new SecretEncryptionConfiguration(encryptionSecret, algorithm, encryptionMethodAlg));

    }

    private void configureJwtSigning(final int signingSecretSize, final String signingAlgorithm, final JwtGenerator<CommonProfile> g) {
        if (signingSecretSize <= 0 || StringUtils.isBlank(signingAlgorithm)) {
            LOGGER.info("No signing algorithm or size specified, so the generated JWT will not be encrypted");
            return;
        }

        final var signingSecret = RandomStringUtils.randomAlphanumeric(signingSecretSize);
        LOGGER.info("==== Signing Secret ====\n{}\n", signingSecret);

        final var acceptedSigningAlgs = Arrays.stream(JWSAlgorithm.class.getDeclaredFields())
            .filter(f -> f.getType().equals(JWSAlgorithm.class))
            .map(Unchecked.function(f -> {
                f.setAccessible(true);
                return ((JWSAlgorithm) f.get(null)).getName();
            }))
            .collect(Collectors.joining(","));
        LOGGER.debug("Signing algorithm: [{}]. Available algorithms are [{}]", signingAlgorithm, acceptedSigningAlgs);

        g.setSignatureConfiguration(new SecretSignatureConfiguration(signingSecret, JWSAlgorithm.parse(signingAlgorithm)));
    }
}
