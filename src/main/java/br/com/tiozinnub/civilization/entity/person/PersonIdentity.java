package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.entity.property.Gender;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;

import static br.com.tiozinnub.civilization.config.CivilizationModConfig.getPersonNamesConfig;

public class PersonIdentity extends Serializable {
    private String firstName;
    private String lastName;
    private Gender gender;
    private Species species;

    public PersonIdentity(NbtCompound nbtCompound) {
        this.fromNbt(nbtCompound);
    }

    public PersonIdentity(String firstName, String lastName, Gender gender, Species species) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.species = species;
    }

    public static PersonIdentity empty() {
        return new PersonIdentity("Name", "LastName", Gender.MALE, Species.HUMAN);
    }

    public static PersonIdentity randomize(Random random) {
        var gender = Gender.randomGender(random);

        return new PersonIdentity(
                getPersonNamesConfig().getRandomFirstName(random, gender),
                getPersonNamesConfig().getRandomLastName(random),
                gender,
                Species.randomSpecies(random)
        );
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("firstName", () -> this.firstName, (value) -> this.firstName = value, null);
        helper.registerProperty("lastName", () -> this.lastName, (value) -> this.lastName = value, null);
        helper.registerProperty("gender", () -> this.gender.asString(), (value) -> this.gender = Gender.fromString(value), Gender.MALE.asString());
        helper.registerProperty("species", () -> this.species.asString(), (value) -> this.species = Species.fromString(value), Species.HUMAN.asString());
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public Species getSpecies() {
        return species;
    }

    public String getFullName() {
        return getPersonNamesConfig().invertedNames ?
                "%s, %s".formatted(getLastName(), getFirstName()) :
                "%s %s".formatted(getFirstName(), getLastName());
    }
}
