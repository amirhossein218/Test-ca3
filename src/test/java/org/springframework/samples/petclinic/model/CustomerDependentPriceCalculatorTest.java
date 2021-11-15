package org.springframework.samples.petclinic.model;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.samples.petclinic.model.priceCalculators.CustomerDependentPriceCalculator;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link CustomerDependentPriceCalculator}
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerDependentPriceCalculatorTest {

	private static final double BASE_CHARGE = 1000;
	private static final double BASE_PRICE_PER_PET = 25;
	private static final double DELTA = 0.001;
	private static final UserType USER_TYPE_NEW = UserType.NEW;
	private static final UserType USER_TYPE_GOLD = UserType.GOLD;
	private static final double BASE_RARE_COEF = 1.2;
	private static final double RARE_INFANCY_COEF = 1.4;
	private static final double COMMON_INFANCY_COEF = 1.2;


	private static final CustomerDependentPriceCalculator customerDependentPriceCalculator = new CustomerDependentPriceCalculator();

	private static final Pet pet_rare = mock(Pet.class);
	private static final Pet pet_common = mock(Pet.class);
	private static final Pet pet_rare_infant = mock(Pet.class);
	private static final Pet pet_common_infant = mock(Pet.class);
	private static final PetType pet_type_rare = mock(PetType.class);
	private static final PetType pet_type_common = mock(PetType.class);

	private static List<Pet> pet_common_list;

	private static final PetType rarePetType = mock(PetType.class);
	private static final PetType commonPetType = mock(PetType.class);
	private static final Pet rarePet = mock(Pet.class);
	private static final Pet commonPet = mock(Pet.class);
	private static final Pet rareInfantPet = mock(Pet.class);
	private static final Pet commonInfantPet = mock(Pet.class);

	@BeforeClass
	public static void setup() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 1997);
		final Date adultPetBirthDate = calendar.getTime();
		final Date infantPetBirthDate = new Date();

		when(pet_type_common.getRare()).thenReturn(false);
		when(pet_type_rare.getRare()).thenReturn(true);

		when(pet_rare.getType()).thenReturn(pet_type_rare);
		when(pet_rare.getBirthDate()).thenReturn(adultPetBirthDate);

		when(pet_common.getType()).thenReturn(pet_type_common);
		when(pet_common.getBirthDate()).thenReturn(adultPetBirthDate);

		when(pet_rare_infant.getType()).thenReturn(pet_type_rare);
		when(pet_rare_infant.getBirthDate()).thenReturn(infantPetBirthDate);

		when(pet_common_infant.getType()).thenReturn(pet_type_common);
		when(pet_common_infant.getBirthDate()).thenReturn(infantPetBirthDate);

		pet_common_list = Arrays.asList(pet_common, pet_common, pet_common, pet_common, pet_common,
		pet_common, pet_common, pet_common, pet_common, pet_common, pet_common, pet_common, pet_common, pet_common);
	}

	@Test
	public void calcPriceShouldReturnBaseChargeIfThereIsNoPetForUserTypeNew() {
		double result_price = customerDependentPriceCalculator.calcPrice(Collections.emptyList(), BASE_CHARGE,	BASE_PRICE_PER_PET, USER_TYPE_NEW);
		double target_price = BASE_CHARGE;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldReturnBaseChargeIfThereIsNoPetForUserTypeGold() {
		double result_price = customerDependentPriceCalculator.calcPrice(Collections.emptyList(), BASE_CHARGE,	BASE_PRICE_PER_PET, USER_TYPE_GOLD);
		double target_price = BASE_CHARGE;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldNotUseDiscountForUserTypeNewInDiscountMinScoreNotOccurs() {
		double result_price = customerDependentPriceCalculator.calcPrice(Collections.singletonList(pet_common), BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_NEW);
		double target_price = BASE_CHARGE + BASE_PRICE_PER_PET;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldUseDiscountForUserTypeGoldInDiscountMinScoreNotOccurs() {
		double result_price = customerDependentPriceCalculator.calcPrice(Collections.singletonList(pet_common), BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_GOLD);
		double target_price = BASE_CHARGE + USER_TYPE_GOLD.discountRate * BASE_PRICE_PER_PET;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldNotUseDiscountOnBaseChargeForUserTypeNewInDiscountMinScoreOccurs() {
		double result_price = customerDependentPriceCalculator.calcPrice(pet_common_list, BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_NEW);
		double target_price = BASE_CHARGE + (pet_common_list.size() * BASE_PRICE_PER_PET * USER_TYPE_NEW.discountRate);
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldUseDiscountAtAllForUserTypeGoldInDiscountMinScoreOccurs() {
		double result_price = customerDependentPriceCalculator.calcPrice(pet_common_list, BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_GOLD);
		double target_price = (BASE_CHARGE + pet_common_list.size() * BASE_PRICE_PER_PET) * USER_TYPE_GOLD.discountRate;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldUseCommonInfancyCoefForCommonPetCommonInfant() {
		double result_price = customerDependentPriceCalculator.calcPrice(Collections.singletonList(pet_common_infant),	BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_NEW);
		double target_price = BASE_CHARGE + COMMON_INFANCY_COEF * BASE_PRICE_PER_PET;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldUseRareCoefForPetRare() {
		double result_price = customerDependentPriceCalculator.calcPrice(Collections.singletonList(pet_rare), BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_NEW);
		double target_price = BASE_CHARGE + BASE_RARE_COEF * BASE_PRICE_PER_PET;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldUseRareCoefAndRareInfancyCoefForPetRareInfant() {
		double result_price = customerDependentPriceCalculator.calcPrice(Collections.singletonList(pet_rare_infant), BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_NEW);
		double target_price = BASE_CHARGE + BASE_RARE_COEF * RARE_INFANCY_COEF * BASE_PRICE_PER_PET;
		assertEquals(target_price, result_price, DELTA);
	}
}
