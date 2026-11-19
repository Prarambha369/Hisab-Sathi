import com.prarambha.hisabsathi.presentation.common.icons.CategoryMapping
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CategoryMappingTest {

    private fun category(merchant: String) = CategoryMapping.getCategory(merchant)

    @Test
    fun `nepali food merchants map to Food & Drinks`() {
        assertEquals("Food & Drinks", category("Foodmandu"))
        assertEquals("Food & Drinks", category("PATHAO FOOD"))
        assertEquals("Food & Drinks", category("Himalayan Java Coffee"))
        assertEquals("Food & Drinks", category("Bota Momo Thamel"))
        assertEquals("Food & Drinks", category("Bajeko Sekuwa"))
    }

    @Test
    fun `nepali groceries map to Groceries, not Shopping`() {
        assertEquals("Groceries", category("Bhatbhateni Supermarket"))
        assertEquals("Groceries", category("Big Mart Baluwatar"))
        assertEquals("Groceries", category("Salesberry"))
    }

    @Test
    fun `transport, bills and top-ups`() {
        assertEquals("Transport", category("Pathao"))
        assertEquals("Transport", category("inDrive"))
        assertEquals("Transport", category("Nepal Oil Corporation"))
        assertEquals("Bill", category("NEA Electricity"))
        assertEquals("Bill", category("WorldLink Communications"))
        assertEquals("Bill", category("KUKL Khanepani"))
        assertEquals("Top-up", category("NTC Recharge"))
        assertEquals("Top-up", category("Ncell"))
    }

    @Test
    fun `shopping, entertainment, medical and travel`() {
        assertEquals("Shopping", category("Daraz"))
        assertEquals("Shopping", category("Hamrobazar"))
        assertEquals("Entertainment", category("QFX Cinemas"))
        assertEquals("Entertainment", category("Netflix"))
        assertEquals("Medical", category("Norvic International Hospital"))
        assertEquals("Medical", category("Nepal Mediciti"))
        assertEquals("Travel", category("Buddha Air"))
        assertEquals("Travel", category("Yeti Airlines"))
        assertEquals("Travel", category("Hotel Yak & Yeti"))
    }

    @Test
    fun `tax, investment, insurance and banking`() {
        assertEquals("Tax", category("IRD Income Tax"))
        assertEquals("Investment", category("MeroShare CDSC"))
        assertEquals("Insurance", category("Nepal Life Insurance"))
        assertEquals("Business", category("ConnectIPS"))
    }

    @Test
    fun `air ticket goes to Travel, not Entertainment`() {
        assertEquals("Travel", category("Air Ticket Kathmandu"))
    }

    @Test
    fun `unknown merchants fall back to Miscellaneous`() {
        assertEquals("Miscellaneous", category("Twin Made"))
    }

    @Test
    fun `no keyword is assigned to two categories`() {
        assertTrue(CategoryMapping.findDuplicateKeywords().isEmpty())
    }
}
