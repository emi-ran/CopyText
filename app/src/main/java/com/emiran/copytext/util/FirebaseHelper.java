package com.emiran.copytext.util;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.emiran.copytext.model.EncryptedClipboardItem;
import com.emiran.copytext.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final String USERS_COLLECTION = "users";
    private static final String CLIPBOARD_COLLECTION = "clipboard_items";

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final EncryptionHelper encryptionHelper;

    public FirebaseHelper(Context context) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        encryptionHelper = new EncryptionHelper(context);
    }

    // Kullanıcı işlemleri
    public Task<AuthResult> signUp(String email, String password, String name) {
        return auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser != null) {
                        User user = new User(firebaseUser.getUid(), email, name);
                        db.collection(USERS_COLLECTION)
                                .document(firebaseUser.getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error creating user profile", e));
                    }
                });
    }

    public Task<AuthResult> signIn(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public void signOut() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public Task<User> getUserProfile() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new IllegalStateException("No user logged in"));
        }

        return db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(User.class);
                    }
                    return null;
                });
    }

    // Pano öğesi işlemleri
    public Task<DocumentReference> saveClipboardItem(String text) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new IllegalStateException("No user logged in"));
        }

        String iv = encryptionHelper.generateIv();
        String encryptedText = encryptionHelper.encrypt(text, currentUser.getUid());
        
        EncryptedClipboardItem item = new EncryptedClipboardItem(
                currentUser.getUid(),
                encryptedText,
                iv
        );

        return db.collection(CLIPBOARD_COLLECTION)
                .add(item)
                .addOnSuccessListener(ref -> {
                    item.setId(ref.getId());
                    ref.set(item);
                });
    }

    public Task<List<EncryptedClipboardItem>> getClipboardItems() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new IllegalStateException("No user logged in"));
        }

        return db.collection(CLIPBOARD_COLLECTION)
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObjects(EncryptedClipboardItem.class);
                    }
                    return null;
                });
    }

    public String decryptText(EncryptedClipboardItem item) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(item.getUserId())) {
            return null;
        }

        return encryptionHelper.decrypt(item.getEncryptedText(), currentUser.getUid(), item.getIv());
    }

    public Task<Void> deleteClipboardItem(String itemId) {
        return db.collection(CLIPBOARD_COLLECTION)
                .document(itemId)
                .delete();
    }

    public Task<Void> clearClipboardHistory() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new IllegalStateException("No user logged in"));
        }

        return db.collection(CLIPBOARD_COLLECTION)
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentReference> refs = task.getResult().getDocuments().stream()
                                .map(doc -> db.collection(CLIPBOARD_COLLECTION).document(doc.getId()))
                                .toList();
                        
                        return FirebaseFirestore.getInstance().runBatch(batch -> {
                            for (DocumentReference ref : refs) {
                                batch.delete(ref);
                            }
                        });
                    }
                    return null;
                });
    }
} 