package sa.edu.kau.fcit.cpit252.carwash.database;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Map;

import sa.edu.kau.fcit.cpit252.carwash.models.Customer;
import sa.edu.kau.fcit.cpit252.carwash.models.User;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DatabaseAndOrderManagerTest {

    @Before
    public void resetState() throws Exception {
        resetDatabaseManagerSingleton();
        DataStore.setCurrentUser(null);
    }

    @Test
    public void databaseManager_shouldCreateOneSingletonAndReturnFirebaseServices() throws Exception {
        FirebaseAuth auth = mock(FirebaseAuth.class);
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);

        try (MockedStatic<FirebaseAuth> authStatic = Mockito.mockStatic(FirebaseAuth.class);
             MockedStatic<FirebaseFirestore> firestoreStatic = Mockito.mockStatic(FirebaseFirestore.class)) {

            authStatic.when(FirebaseAuth::getInstance).thenReturn(auth);
            firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(firestore);

            DatabaseManager first = DatabaseManager.getInstance();
            DatabaseManager second = DatabaseManager.getInstance();

            assertSame(first, second);
            assertSame(auth, first.getAuth());
            assertSame(firestore, first.getDb());

            authStatic.verify(FirebaseAuth::getInstance, Mockito.times(1));
            firestoreStatic.verify(FirebaseFirestore::getInstance, Mockito.times(1));
        }
    }

    @Test
    public void dataStore_shouldStoreReplaceAndReturnCurrentUser() {
        User firstUser = new Customer("Nora", "Ali", "nora@example.com", "123456");
        User secondUser = new Customer("Sara", "Saleh", "sara@example.com", "abcdef");

        DataStore.setCurrentUser(firstUser);
        assertSame(firstUser, DataStore.getCurrentUser());

        DataStore.setCurrentUser(secondUser);
        assertSame(secondUser, DataStore.getCurrentUser());
        assertNotSame(firstUser, DataStore.getCurrentUser());
    }

    @Test
    public void dataStore_logoutUser_shouldClearCurrentUserAndSignOutFirebaseAuth() {
        DatabaseManager manager = mock(DatabaseManager.class);
        FirebaseAuth auth = mock(FirebaseAuth.class);
        when(manager.getAuth()).thenReturn(auth);

        try (MockedStatic<DatabaseManager> managerStatic = Mockito.mockStatic(DatabaseManager.class)) {
            managerStatic.when(DatabaseManager::getInstance).thenReturn(manager);

            User currentUser = new Customer("Nora", "Ali", "nora@example.com", "123456");
            DataStore.setCurrentUser(currentUser);

            DataStore.logoutUser();

            assertNull(DataStore.getCurrentUser());
            verify(auth).signOut();
        }
    }

    @Test
    public void saveNewOrder_shouldCreateOrderWithExpectedFieldsAndShortCode() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference ordersCollection = mock(CollectionReference.class);
        DocumentReference newOrderRef = mock(DocumentReference.class);
        @SuppressWarnings("unchecked")
        Task<Void> setTask = mock(Task.class);
        @SuppressWarnings("unchecked")
        OnCompleteListener<Void> completionListener = mock(OnCompleteListener.class);

        setupDatabaseManagerStaticMock(firestore, () -> {
            when(firestore.collection("Orders")).thenReturn(ordersCollection);
            when(ordersCollection.document()).thenReturn(newOrderRef);
            when(newOrderRef.getId()).thenReturn("abcdef123456");
            when(newOrderRef.set(anyMap())).thenReturn(setTask);
            when(setTask.addOnCompleteListener(eq(completionListener))).thenReturn(setTask);

            OrderManager.saveNewOrder(
                    "user-1",
                    "Full Service",
                    "SAR 200.0",
                    "SUV",
                    completionListener
            );

            ArgumentCaptor<Map> orderCaptor = ArgumentCaptor.forClass(Map.class);
            verify(newOrderRef).set(orderCaptor.capture());

            Map order = orderCaptor.getValue();
            assertEquals("user-1", order.get("userId"));
            assertEquals("Full Service", order.get("packageName"));
            assertEquals("SAR 200.0", order.get("packagePrice"));
            assertEquals("SUV", order.get("vehicle"));
            assertEquals("0", order.get("washesUsed"));
            assertEquals("5", order.get("maxWashes"));
            assertEquals("ABCDEF", order.get("shortCode"));
            assertEquals("active", order.get("status"));
            assertTrue(order.containsKey("createdAt"));

            verify(setTask).addOnCompleteListener(completionListener);
        });
    }

    @Test
    public void deductWash_existingOrder_shouldIncreaseUsedWashesAndCallSuccess() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference ordersCollection = mock(CollectionReference.class);
        DocumentReference orderRef = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        @SuppressWarnings("unchecked")
        Task<DocumentSnapshot> getTask = mock(Task.class);
        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);
        OrderManager.OperationCallback callback = mock(OrderManager.OperationCallback.class);

        setupDatabaseManagerStaticMock(firestore, () -> {
            when(firestore.collection("Orders")).thenReturn(ordersCollection);
            when(ordersCollection.document("order-1")).thenReturn(orderRef);
            when(orderRef.get()).thenReturn(getTask);
            when(snapshot.exists()).thenReturn(true);
            when(snapshot.getString("washesUsed")).thenReturn("2");
            when(snapshot.getString("maxWashes")).thenReturn("5");
            when(orderRef.update(anyMap())).thenReturn(updateTask);

            callSuccessWhenGetCompletes(getTask, snapshot);
            callSuccessWhenUpdateCompletes(updateTask);

            new OrderManager().deductWash("order-1", callback);

            ArgumentCaptor<Map> updatesCaptor = ArgumentCaptor.forClass(Map.class);
            verify(orderRef).update(updatesCaptor.capture());

            Map updates = updatesCaptor.getValue();
            assertEquals("3", updates.get("washesUsed"));
            assertFalse(updates.containsKey("status"));
            verify(callback).onSuccess();
            verify(callback, never()).onFailure(anyString());
        });
    }

    @Test
    public void deductWash_lastRemainingWash_shouldMarkOrderCompleted() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference ordersCollection = mock(CollectionReference.class);
        DocumentReference orderRef = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        @SuppressWarnings("unchecked")
        Task<DocumentSnapshot> getTask = mock(Task.class);
        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);
        OrderManager.OperationCallback callback = mock(OrderManager.OperationCallback.class);

        setupDatabaseManagerStaticMock(firestore, () -> {
            when(firestore.collection("Orders")).thenReturn(ordersCollection);
            when(ordersCollection.document("order-1")).thenReturn(orderRef);
            when(orderRef.get()).thenReturn(getTask);
            when(snapshot.exists()).thenReturn(true);
            when(snapshot.getString("washesUsed")).thenReturn("4");
            when(snapshot.getString("maxWashes")).thenReturn("5");
            when(orderRef.update(anyMap())).thenReturn(updateTask);

            callSuccessWhenGetCompletes(getTask, snapshot);
            callSuccessWhenUpdateCompletes(updateTask);

            new OrderManager().deductWash("order-1", callback);

            ArgumentCaptor<Map> updatesCaptor = ArgumentCaptor.forClass(Map.class);
            verify(orderRef).update(updatesCaptor.capture());

            Map updates = updatesCaptor.getValue();
            assertEquals("5", updates.get("washesUsed"));
            assertEquals("completed", updates.get("status"));
            verify(callback).onSuccess();
        });
    }

    @Test
    public void deductWash_missingOrder_shouldCallFailureAndNotUpdate() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference ordersCollection = mock(CollectionReference.class);
        DocumentReference orderRef = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        @SuppressWarnings("unchecked")
        Task<DocumentSnapshot> getTask = mock(Task.class);
        OrderManager.OperationCallback callback = mock(OrderManager.OperationCallback.class);

        setupDatabaseManagerStaticMock(firestore, () -> {
            when(firestore.collection("Orders")).thenReturn(ordersCollection);
            when(ordersCollection.document("missing-order")).thenReturn(orderRef);
            when(orderRef.get()).thenReturn(getTask);
            when(snapshot.exists()).thenReturn(false);

            callSuccessWhenGetCompletes(getTask, snapshot);

            new OrderManager().deductWash("missing-order", callback);

            verify(callback).onFailure("Order not found");
            verify(callback, never()).onSuccess();
            verify(orderRef, never()).update(anyMap());
        });
    }

    @Test
    public void deductWash_whenNoWashesRemain_shouldCallFailureAndNotUpdate() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference ordersCollection = mock(CollectionReference.class);
        DocumentReference orderRef = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        @SuppressWarnings("unchecked")
        Task<DocumentSnapshot> getTask = mock(Task.class);
        OrderManager.OperationCallback callback = mock(OrderManager.OperationCallback.class);

        setupDatabaseManagerStaticMock(firestore, () -> {
            when(firestore.collection("Orders")).thenReturn(ordersCollection);
            when(ordersCollection.document("order-1")).thenReturn(orderRef);
            when(orderRef.get()).thenReturn(getTask);
            when(snapshot.exists()).thenReturn(true);
            when(snapshot.getString("washesUsed")).thenReturn("5");
            when(snapshot.getString("maxWashes")).thenReturn("5");

            callSuccessWhenGetCompletes(getTask, snapshot);

            new OrderManager().deductWash("order-1", callback);

            verify(callback).onFailure("No washes remaining on this package");
            verify(callback, never()).onSuccess();
            verify(orderRef, never()).update(anyMap());
        });
    }

    @Test
    public void saveWashReport_shouldSaveExpectedReportAndCallSuccess() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference reportsCollection = mock(CollectionReference.class);
        DocumentReference reportRef = mock(DocumentReference.class);
        @SuppressWarnings("unchecked")
        Task<DocumentReference> addTask = mock(Task.class);
        OrderManager.OperationCallback callback = mock(OrderManager.OperationCallback.class);

        setupDatabaseManagerStaticMock(firestore, () -> {
            when(firestore.collection("WashReports")).thenReturn(reportsCollection);
            when(reportsCollection.add(anyMap())).thenReturn(addTask);

            doAnswer(invocation -> {
                OnSuccessListener<DocumentReference> listener = invocation.getArgument(0);
                listener.onSuccess(reportRef);
                return addTask;
            }).when(addTask).addOnSuccessListener(any());
            when(addTask.addOnFailureListener(any())).thenReturn(addTask);

            new OrderManager().saveWashReport(
                    "Customer Name",
                    "Sedan",
                    "Interior",
                    "Cashier Name",
                    callback
            );

            ArgumentCaptor<Map> reportCaptor = ArgumentCaptor.forClass(Map.class);
            verify(reportsCollection).add(reportCaptor.capture());

            Map report = reportCaptor.getValue();
            assertEquals("Customer Name", report.get("customerName"));
            assertEquals("Sedan", report.get("vehicle"));
            assertEquals("Interior", report.get("packageName"));
            assertEquals("Cashier Name", report.get("cashierName"));
            assertTrue(report.containsKey("timestamp"));
            verify(callback).onSuccess();
            verify(callback, never()).onFailure(anyString());
        });
    }

    private void setupDatabaseManagerStaticMock(FirebaseFirestore firestore, Runnable testBody) {
        DatabaseManager manager = mock(DatabaseManager.class);
        when(manager.getDb()).thenReturn(firestore);

        try (MockedStatic<DatabaseManager> managerStatic = Mockito.mockStatic(DatabaseManager.class)) {
            managerStatic.when(DatabaseManager::getInstance).thenReturn(manager);
            testBody.run();
        }
    }

    private void callSuccessWhenGetCompletes(Task<DocumentSnapshot> getTask, DocumentSnapshot snapshot) {
        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(snapshot);
            return getTask;
        }).when(getTask).addOnSuccessListener(any());
        when(getTask.addOnFailureListener(any())).thenReturn(getTask);
    }

    private void callSuccessWhenUpdateCompletes(Task<Void> updateTask) {
        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        }).when(updateTask).addOnSuccessListener(any());
        when(updateTask.addOnFailureListener(any())).thenReturn(updateTask);
    }

    private void resetDatabaseManagerSingleton() throws Exception {
        Field instanceField = DatabaseManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}
