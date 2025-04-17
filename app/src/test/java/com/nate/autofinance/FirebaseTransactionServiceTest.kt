import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.remote.FirebasePeriodService
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.domain.models.FinancialPeriod
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class FinancialPeriodRepositoryTest {

    @Mock
    private lateinit var periodDao: FinancialPeriodDao

    @Mock
    private lateinit var firebasePeriodService: FirebasePeriodService

    // Usaremos um dispatcher de teste, por exemplo o UnconfinedTestDispatcher para execução imediata
    private val testDispatcher = UnconfinedTestDispatcher()

    // Instância do repositório sob teste
    private lateinit var repository: PeriodRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = PeriodRepository(
            periodDao = periodDao,
            firebasePeriodService = firebasePeriodService,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `addFinancialPeriod - sucesso no envio para Firebase deve atualizar com firebaseDocId e SYNCED`() = runBlocking {
        // Arrange: cria um FinancialPeriod com syncStatus PENDING (inicial)
        val basePeriod = FinancialPeriod(
            id = 0,
            startDate = Date(),
            endDate = null,
            totalIncome = 200.0,
            totalExpenses = 50.0,
            userId = 1,
            syncStatus = SyncStatus.PENDING,
            firebaseDocUserId = null,
            firebaseDocId = null
        )
        // Simula a inserção local retornando 1 (como Long)
        `when`(periodDao.insert(basePeriod)).thenReturn(1L)
        // Simula o envio para o Firebase retornando um firebaseDocId gerado
        val generatedFirebaseDocId = "firebase123"
        `when`(firebasePeriodService.addFinancialPeriod(basePeriod)).thenReturn(generatedFirebaseDocId)

        // Act: chama o método de adicionar período
        repository.addFinancialPeriod(basePeriod)

        // Assert: espera que o objeto atualizado seja enviado para o update do DAO com o firebaseDocId e status SYNCED
        val expectedUpdatedPeriod = basePeriod.copy(
            id = 1,
            firebaseDocId = generatedFirebaseDocId,
            syncStatus = SyncStatus.SYNCED
        )
        verify(periodDao).update(expectedUpdatedPeriod)
    }

    @Test
    fun `addFinancialPeriod - falha no envio para Firebase deve atualizar com status FAILED`() = runBlocking {
        // Arrange: cria um FinancialPeriod
        val basePeriod = FinancialPeriod(
            id = 0,
            startDate = Date(),
            endDate = null,
            totalIncome = 200.0,
            totalExpenses = 50.0,
            userId = 1,
            syncStatus = SyncStatus.PENDING,
            firebaseDocUserId = null,
            firebaseDocId = null
        )
        `when`(periodDao.insert(basePeriod)).thenReturn(1L)
        // Simula que a chamada para o Firebase lança exceção
        `when`(firebasePeriodService.addFinancialPeriod(basePeriod))
            .thenThrow(RuntimeException("Firebase error"))

        // Act
        repository.addFinancialPeriod(basePeriod)

        // Assert: deve atualizar o período com syncStatus FAILED
        val expectedUpdatedPeriod = basePeriod.copy(
            id = 1,
            syncStatus = SyncStatus.FAILED
        )
        verify(periodDao).update(expectedUpdatedPeriod)
    }

    @Test
    fun `updateFinancialPeriod - sucesso na atualização remota deve atualizar com SYNCED`() = runBlocking {
        // Arrange: cria um FinancialPeriod já inserido e com firebaseDocId
        val basePeriod = FinancialPeriod(
            id = 1,
            startDate = Date(),
            endDate = null,
            totalIncome = 200.0,
            totalExpenses = 50.0,
            userId = 1,
            syncStatus = SyncStatus.PENDING,
            firebaseDocUserId = "userFirebase1",
            firebaseDocId = "firebase123"
        )
        // O método updateFinancialPeriod do Firebase é chamado e não lança exceção

        // Act
        repository.updateFinancialPeriod(basePeriod)

        // Assert:
        // O DAO deve ser chamado com o período atualizado com status SYNCED.
        val expectedUpdatedPeriod = basePeriod.copy(syncStatus = SyncStatus.SYNCED)
        verify(periodDao).update(expectedUpdatedPeriod)
    }

    @Test
    fun `updateFinancialPeriod - falha na atualização remota deve atualizar com FAILED`() = runBlocking {
        // Arrange
        val basePeriod = FinancialPeriod(
            id = 1,
            startDate = Date(),
            endDate = null,
            totalIncome = 200.0,
            totalExpenses = 50.0,
            userId = 1,
            syncStatus = SyncStatus.PENDING,
            firebaseDocUserId = "userFirebase1",
            firebaseDocId = "firebase123"
        )
        // Simula que a atualização remota lança exceção
        doThrow(RuntimeException("Firebase error"))
            .`when`(firebasePeriodService)
            .updateFinancialPeriod(eq("firebase123"), anyMap())

        // Act
        repository.updateFinancialPeriod(basePeriod)

        // Assert: deve atualizar com syncStatus FAILED
        val expectedUpdatedPeriod = basePeriod.copy(syncStatus = SyncStatus.FAILED)
        verify(periodDao).update(expectedUpdatedPeriod)
    }

    @Test
    fun `deleteFinancialPeriod - deve deletar localmente e na nuvem se firebaseDocId existir`() = runBlocking {
        // Arrange
        val basePeriod = FinancialPeriod(
            id = 1,
            startDate = Date(),
            endDate = null,
            totalIncome = 200.0,
            totalExpenses = 50.0,
            userId = 1,
            syncStatus = SyncStatus.SYNCED,
            firebaseDocUserId = "userFirebase1",
            firebaseDocId = "firebase123"
        )

        // Act
        repository.deleteFinancialPeriod(basePeriod)

        // Assert: verifica se o DAO deletou localmente e se o serviço do Firebase foi chamado
        verify(periodDao).delete(basePeriod)
        verify(firebasePeriodService).deleteFinancialPeriod("firebase123")
    }

    @Test
    fun `getFinancialPeriodsForUser - deve retornar a lista proveniente do DAO`() = runBlocking {
        // Arrange
        val userId = 1
        val periodList = listOf(
            FinancialPeriod(
                id = 1,
                startDate = Date(),
                endDate = null,
                totalIncome = 200.0,
                totalExpenses = 50.0,
                userId = 1,
                syncStatus = SyncStatus.SYNCED,
                firebaseDocUserId = "userFirebase1",
                firebaseDocId = "firebase123"
            )
        )
        `when`(periodDao.getPeriodsByUserId(userId)).thenReturn(periodList)

        // Act
        val result = repository.getFinancialPeriodsForUser(userId)

        // Assert: a lista retornada deve ser igual à do DAO
        assertEquals(periodList, result)
    }
}
