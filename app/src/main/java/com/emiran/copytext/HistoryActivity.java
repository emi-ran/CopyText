package com.emiran.copytext;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emiran.copytext.adapter.ClipboardAdapter;
import com.emiran.copytext.model.ClipboardItem;
import com.emiran.copytext.repository.ClipboardRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.List;
import android.view.LayoutInflater;

public class HistoryActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private TextView emptyText;
    private ClipboardAdapter adapter;
    private ClipboardRepository repository;
    private ExtendedFloatingActionButton deleteSelectedFab;
    private Set<ClipboardItem> selectedItems = new HashSet<>();
    private boolean isSearchMode = false;
    private String currentSearchQuery = "";
    private List<ClipboardItem> allItems = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Karanlık tema ayarla
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        
        setContentView(R.layout.activity_history);
        
        // Toolbar'ı ayarla
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        
        recyclerView = findViewById(R.id.recyclerView);
        emptyText = findViewById(R.id.emptyText);
        deleteSelectedFab = findViewById(R.id.deleteSelectedFab);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClipboardAdapter();
        recyclerView.setAdapter(adapter);
        
        repository = new ClipboardRepository(getApplication());
        
        // Arama modundan çıkış için tıklama dinleyicileri
        recyclerView.setOnTouchListener((v, event) -> {
            if (isSearchMode && event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                exitSearchMode();
                return true;
            }
            return false;
        });
        
        emptyText.setOnClickListener(v -> {
            if (isSearchMode) {
                exitSearchMode();
            }
        });
        
        // Adapter click listener'ları
        adapter.setOnItemClickListener(new ClipboardAdapter.OnItemClickListener() {
            @Override
            public void onCopyClick(ClipboardItem item) {
                if (isSearchMode) {
                    exitSearchMode();
                }
                copyToClipboard(item.getText());
            }
            
            @Override
            public void onDeleteClick(ClipboardItem item) {
                if (isSearchMode) {
                    exitSearchMode();
                }
                showDeleteDialog(item);
            }
            
            @Override
            public void onPinClick(ClipboardItem item) {
                if (isSearchMode) {
                    exitSearchMode();
                }
                togglePin(item);
            }
            
            @Override
            public void onItemChecked(ClipboardItem item, boolean isChecked) {
                if (isSearchMode) {
                    exitSearchMode();
                }
                if (isChecked) {
                    selectedItems.add(item);
                } else {
                    selectedItems.remove(item);
                }
                updateDeleteFabVisibility();
            }
        });
        
        deleteSelectedFab.setOnClickListener(v -> {
            if (isSearchMode) {
                exitSearchMode();
            }
            showDeleteSelectedDialog();
        });
        
        loadHistory();
    }
    
    private void updateDeleteFabVisibility() {
        if (selectedItems.isEmpty()) {
            deleteSelectedFab.setVisibility(View.GONE);
        } else {
            deleteSelectedFab.setVisibility(View.VISIBLE);
        }
        
        // Menüyü yenile
        invalidateOptionsMenu();
    }
    
    private void showDeleteSelectedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_selected_title)
                .setMessage(getString(R.string.delete_selected_message, selectedItems.size()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    deleteSelectedItems();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
    
    private void deleteSelectedItems() {
        for (ClipboardItem item : selectedItems) {
            repository.delete(item);
        }
        selectedItems.clear();
        loadHistory(); // allItems'ı güncelle
        Toast.makeText(this, R.string.selected_items_deleted, Toast.LENGTH_SHORT).show();
    }
    
    private void loadHistory() {
        repository.getAllItems().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                adapter.setItems(items);
                recyclerView.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);
                allItems = new ArrayList<>(items); // Tüm öğeleri tut
                
                // Debug log
                // android.util.Log.d("LoadHistory", "Loaded " + allItems.size() + " items");
                // for (ClipboardItem item : allItems) {
                //     android.util.Log.d("LoadHistory", "Item: '" + item.getTitle() + "' - '" + item.getText() + "'");
                // }
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyText.setVisibility(View.VISIBLE);
                allItems.clear(); // Tüm öğeleri temizle
                // android.util.Log.d("LoadHistory", "No items found");
            }
        });
    }
    
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.text_copied_again, Toast.LENGTH_SHORT).show();
    }
    
    private void showDeleteDialog(ClipboardItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Öğeyi Sil")
                .setMessage("Bu öğeyi silmek istediğinizden emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    repository.delete(item);
                    loadHistory(); // allItems'ı güncelle
                    Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hayır", null)
                .show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        
        // Edit ve rename menü öğelerini başlangıçta gizle
        menu.findItem(R.id.action_edit).setVisible(false);
        menu.findItem(R.id.action_rename).setVisible(false);
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Arama modunda edit ve rename menü öğelerini gizle
        if (isSearchMode) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_rename).setVisible(false);
            menu.findItem(R.id.action_clear_history).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false); // Arama ikonunu da gizle
        } else {
            // Seçili öğe sayısına göre menü öğelerini göster/gizle
            boolean singleItemSelected = selectedItems.size() == 1;
            menu.findItem(R.id.action_edit).setVisible(singleItemSelected);
            menu.findItem(R.id.action_rename).setVisible(singleItemSelected);
            menu.findItem(R.id.action_clear_history).setVisible(true);
            menu.findItem(R.id.action_search).setVisible(true); // Arama ikonunu göster
        }
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_clear_history) {
            showClearHistoryDialog();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            if (selectedItems.size() == 1) {
                ClipboardItem selectedItem = selectedItems.iterator().next();
                showEditDialog(selectedItem);
            }
            return true;
        } else if (item.getItemId() == R.id.action_rename) {
            if (selectedItems.size() == 1) {
                ClipboardItem selectedItem = selectedItems.iterator().next();
                showRenameDialog(selectedItem);
            }
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            toggleSearchMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void toggleSearchMode() {
        isSearchMode = !isSearchMode;
        
        if (isSearchMode) {
            // Arama modunu aç
            showSearchView();
        } else {
            // Arama modunu kapat
            exitSearchMode();
        }
        
        invalidateOptionsMenu();
    }
    
    private void showSearchView() {
        // Toolbar'ı arama view'ına çevir
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        
        // Arama view'ını oluştur
        androidx.appcompat.widget.SearchView searchView = new androidx.appcompat.widget.SearchView(this);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(currentSearchQuery, false);
        
        // İkinci arama ikonunu gizle
        searchView.setIconified(false);
        searchView.setSubmitButtonEnabled(false);
        
        // Arama alanına otomatik odaklan ve klavyeyi aç
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);
        
        // Klavyeyi açmak için daha güvenilir yöntem
        searchView.post(() -> {
            searchView.requestFocus();
            // Biraz daha gecikme ile klavyeyi aç
            searchView.postDelayed(() -> {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(searchView, android.view.inputmethod.InputMethodManager.SHOW_FORCED);
                }
            }, 300); // 300ms gecikme
        });
        
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filterItems(newText);
                return true;
            }
        });
        
        searchView.setOnCloseListener(() -> {
            hideSearchView();
            return true;
        });
        
        getSupportActionBar().setCustomView(searchView);
    }
    
    private void hideSearchView() {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setCustomView(null);
    }
    
    private void filterItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Arama boşsa tüm öğeleri göster
            adapter.setItems(allItems);
            updateEmptyText(allItems.isEmpty());
        } else {
            // Arama yap - hem sorgu hem içerikleri normalize et
            String normalizedQuery = normalizeTurkishCharacters(query.toLowerCase().trim());
            List<ClipboardItem> filteredItems = new ArrayList<>();
            
            for (ClipboardItem item : allItems) {
                // Başlık ve metni normalize et
                String title = item.getTitle() != null ? normalizeTurkishCharacters(item.getTitle().toLowerCase()) : "";
                String text = item.getText() != null ? normalizeTurkishCharacters(item.getText().toLowerCase()) : "";
                
                // Normalize edilmiş içeriklerde arama yap
                if (title.contains(normalizedQuery) || text.contains(normalizedQuery)) {
                    filteredItems.add(item);
                }
            }
            
            adapter.setItems(filteredItems);
            updateEmptyText(filteredItems.isEmpty());
        }
    }
    
    private String normalizeTurkishCharacters(String text) {
        if (text == null) return "";
        
        // Önce büyük harfleri küçük harfe çevir
        String lowerText = text.toLowerCase();
        
        String result = lowerText
                // i, ı, I, İ -> i (artık hepsi küçük harf)
                .replace("ı", "i")
                // ü, u -> u (artık hepsi küçük harf)
                .replace("ü", "u")
                // ö, o -> o (artık hepsi küçük harf)
                .replace("ö", "o")
                // ç, c -> c (artık hepsi küçük harf)
                .replace("ç", "c")
                // ş, s -> s (artık hepsi küçük harf)
                .replace("ş", "s")
                // ğ, g -> g (artık hepsi küçük harf)
                .replace("ğ", "g");
        
        return result;
    }
    
    private void updateEmptyText(boolean isEmpty) {
        if (isEmpty) {
            if (currentSearchQuery != null && !currentSearchQuery.trim().isEmpty()) {
                emptyText.setText(R.string.no_search_results);
            } else {
                emptyText.setText(R.string.no_history_items);
            }
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }
    
    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_history)
                .setMessage(R.string.clear_history_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    repository.deleteAllNonPinned();
                    loadHistory(); // allItems'ı güncelle
                    Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
    
    private void togglePin(ClipboardItem item) {
        item.setPinned(!item.isPinned());
        repository.update(item);
        loadHistory(); // allItems'ı güncelle
        
        String message = item.isPinned() ? "Öğe sabitlendi" : "Öğe sabitleme kaldırıldı";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void showEditDialog(ClipboardItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        
        com.google.android.material.textfield.TextInputLayout textInputLayout = dialogView.findViewById(R.id.textInputLayout);
        com.google.android.material.textfield.TextInputEditText textInput = dialogView.findViewById(R.id.textInput);
        
        textInputLayout.setHint(getString(R.string.enter_text));
        textInput.setText(item.getText());
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_text)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newText = textInput.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        item.setText(newText);
                        repository.update(item);
                        loadHistory(); // allItems'ı güncelle
                        Toast.makeText(this, R.string.item_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void showRenameDialog(ClipboardItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        
        com.google.android.material.textfield.TextInputLayout textInputLayout = dialogView.findViewById(R.id.textInputLayout);
        com.google.android.material.textfield.TextInputEditText textInput = dialogView.findViewById(R.id.textInput);
        
        textInputLayout.setHint(getString(R.string.enter_title));
        textInput.setText(item.getTitle());
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.rename_item)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newTitle = textInput.getText().toString().trim();
                    item.setTitle(newTitle);
                    repository.update(item);
                    loadHistory(); // allItems'ı güncelle
                    Toast.makeText(this, R.string.item_renamed, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void exitSearchMode() {
        isSearchMode = false;
        hideSearchView();
        currentSearchQuery = "";
        filterItems("");
        invalidateOptionsMenu();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.shutdown();
        }
    }
} 