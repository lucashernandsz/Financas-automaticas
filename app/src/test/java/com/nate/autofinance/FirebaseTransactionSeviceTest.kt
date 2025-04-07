import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.nate.autofinance.data.remote.FirebaseTransactionService
import com.nate.autofinance.domain.models.Transaction
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.*

import java.util.Date

class FirebaseTransactionServiceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var transactionsCollection: CollectionReference
    private lateinit var service: FirebaseTransactionService

    // Mocks para as operações do Firestore
    private lateinit var documentReference: DocumentReference
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var documentSnapshot: DocumentSnapshot
    private lateinit var query: Query

    @Before
    fun setup() {
        // Cria mocks do FirebaseFirestore e da CollectionReference
        firestore = mock(FirebaseFirestore::class.java)
        transactionsCollection = mock(CollectionReference::class.java)
        documentReference = mock(DocumentReference::class.java)
        querySnapshot = mock(QuerySnapshot::class.java)
        documentSnapshot = mock(DocumentSnapshot::class.java)
        query = mock(Query::class.java)

        // Sempre que firestore.collection("transactions") for chamado, retorna o mock da collection.
        `when`(firestore.collection("transactions")).thenReturn(transactionsCollection)

        // Instancia o serviço com o firestore mockado.
        service = FirebaseTransactionService(firestore)
    }

    @Test
    fun `addTransaction deve retornar o id do documento criado`() = runBlocking {
        // Arrange: cria uma transação de exemplo.
        val transaction = Transaction(
            date = Date(),
            amount = 100.0,
            description = "Teste",
            category = "Ganho",
            userId = "user123",
            financialPeriodId = 1,
            imported = false
        )
        val fakeDocId = "docId123"
        // Configura o comportamento: quando add() for chamado, retorna um Task concluído com o documentReference.
        `when`(documentReference.id).thenReturn(fakeDocId)
        `when`(transactionsCollection.add(anyMap<String, Any>())).thenReturn(Tasks.forResult(documentReference))

        // Act: chama o método addTransaction.
        val result = service.addTransaction(transaction)

        // Assert: verifica se o ID retornado é o esperado.
        assertEquals(fakeDocId, result)
    }

    @Test
    fun `updateTransaction deve chamar update com os dados corretos`() {
        runBlocking {
            // Arrange
            val transactionId = "docId123"
            val updatedData = mapOf("amount" to 200.0)
            `when`(transactionsCollection.document(transactionId)).thenReturn(documentReference)
            `when`(documentReference.update(updatedData)).thenReturn(Tasks.forResult(null))

            // Act
            service.updateTransaction(transactionId, updatedData)

            // Assert: verifica se o método update foi chamado com os dados corretos.
            verify(documentReference).update(updatedData)
        }
    }

    @Test
    fun `deleteTransaction deve chamar delete no documento correto`() {
        runBlocking {
            // Arrange
            val transactionId = "docId123"
            `when`(transactionsCollection.document(transactionId)).thenReturn(documentReference)
            `when`(documentReference.delete()).thenReturn(Tasks.forResult(null))

            // Act
            service.deleteTransaction(transactionId)

            // Assert: verifica se o método delete foi chamado.
            verify(documentReference).delete()
        }
    }


    @Test
    fun `getTransactionsForUser deve retornar a lista de transações do usuário`() = runBlocking {
        // Arrange
        val userId = "user123"
        val fakeTransaction = Transaction(
            date = Date(),
            amount = 150.0,
            description = "Compra Teste",
            category = "Despesa",
            userId = userId,
            financialPeriodId = 2,
            imported = false
        )

        // Configura o documentSnapshot para retornar o fakeTransaction ao ser convertido.
        `when`(documentSnapshot.toObject(Transaction::class.java)).thenReturn(fakeTransaction)
        // Configura o querySnapshot para retornar uma lista contendo o documentSnapshot.
        `when`(querySnapshot.documents).thenReturn(listOf(documentSnapshot))
        // Quando for feita a consulta por userId, retorna o mock da Query.
        `when`(transactionsCollection.whereEqualTo("userId", userId)).thenReturn(query)
        // Configura o query.get() para retornar um Task concluído com o querySnapshot.
        `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))

        // Act
        val result = service.getTransactionsForUser(userId)

        // Assert
        assertEquals(1, result.size)
        assertEquals(fakeTransaction, result[0])
    }
}
