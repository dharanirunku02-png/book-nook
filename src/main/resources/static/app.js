/**
 * app.js — Book Nook Frontend
 *
 * How it works:
 *   1. On load: fetch users, populate the user-switcher dropdown
 *   2. On load: fetch all books and render the grid
 *   3. On load: fetch categories and populate the filter dropdown
 *   4. User can search, filter, sort (all call the backend API with query params)
 *   5. Clicking a card opens a detail modal
 *   6. Wishlist add/remove go to the backend; UI updates immediately
 *   7. Dark/light mode is toggled by swapping data-theme on <html>
 */

// ─── App State ──────────────────────────────────────────────────────────────
const state = {
  currentUserId:  null,     // which user is active
  wishlistBookIds: new Set(), // set of book ids in the current user's wishlist
  isOverLimit:    false,    // true if user already has > 10 books
  currentBook:    null,     // book currently shown in the modal
};

// ─── Category Icons (custom inline SVG strings per category) ────────────────
const CATEGORY_ICONS = {
  'Fiction':   '📖',
  'Science':   '🔬',
  'History':   '🏛️',
  'Tech':      '💻',
  'Kids':      '🌈',
  'Self-Help': '🌱',
};

// ─── Spine CSS class per category ───────────────────────────────────────────
function spineClass(cat) {
  const safe = (cat || 'default').replace(/\s+/g, '-');
  return `spine-${safe}`;
}

function badgeClass(cat) {
  const safe = (cat || 'default').replace(/\s+/g, '-');
  return `cat-${safe}`;
}

// ═══════════════════════════════════════════════════════════════════════════
//  INITIALISATION
// ═══════════════════════════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', () => {
  loadUsers();
  loadCategories();
  loadBooks();
});

// ─── Load Users into the Switcher Dropdown ──────────────────────────────────
async function loadUsers() {
  try {
    const users = await apiFetch('/api/users');
    const select = document.getElementById('userSelect');
    users.forEach(u => {
      const opt = document.createElement('option');
      opt.value = u.id;
      opt.textContent = u.name;
      select.appendChild(opt);
    });

    // Auto-select user from ?user=ID URL param or first user
    const urlParams = new URLSearchParams(window.location.search);
    const userParam = urlParams.get('user');
    const selectedId = userParam || (users.length > 0 ? users[0].id : null);
    if (selectedId) {
      select.value = selectedId;
      await switchUser(selectedId);
    }

    // Support #wishlist, #admin, or #admin-add hash
    if (window.location.hash === '#wishlist') {
      showPage('wishlist');
    } else if (window.location.hash === '#admin') {
      showPage('admin');
    } else if (window.location.hash === '#admin-add') {
      showPage('admin');
      openAddBookModal();
    }
  } catch (e) {
    showToast('Could not load users', 'error');
  }
}

// ─── Load Categories into the Filter Dropdown ───────────────────────────────
async function loadCategories() {
  try {
    const cats = await apiFetch('/api/categories');
    const sel = document.getElementById('categoryFilter');
    cats.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c;
      opt.textContent = `${CATEGORY_ICONS[c] || '📚'} ${c}`;
      sel.appendChild(opt);
    });
  } catch (e) {
    console.error('Could not load categories', e);
  }
}

// ─── Load & Render Books ────────────────────────────────────────────────────
async function loadBooks(search = '', category = '', sort = '') {
  showLoading(true);
  try {
    const params = new URLSearchParams();
    if (search)   params.set('search',   search);
    if (category) params.set('category', category);
    if (sort)     params.set('sort',     sort);

    const url = '/api/books' + (params.toString() ? '?' + params.toString() : '');
    const books = await apiFetch(url);

    renderBookGrid(books);
  } catch (e) {
    showToast('Could not load books', 'error');
  } finally {
    showLoading(false);
  }
}

// ─── Render Book Grid ────────────────────────────────────────────────────────
function renderBookGrid(books) {
  const grid  = document.getElementById('bookGrid');
  const noRes = document.getElementById('noResults');

  // Remove old cards but keep the loading state element
  Array.from(grid.children).forEach(c => {
    if (!c.id || c.id !== 'loadingState') grid.removeChild(c);
  });

  if (books.length === 0) {
    noRes.classList.remove('hidden');
    return;
  }
  noRes.classList.add('hidden');

  books.forEach(book => {
    const card = createBookCard(book);
    grid.appendChild(card);
  });
}

// ─── Create a Single Book Card ───────────────────────────────────────────────
function createBookCard(book) {
  const isInWishlist = state.wishlistBookIds.has(book.id);
  const icon = CATEGORY_ICONS[book.category] || '📚';

  // Stock label
  let stockLabel, stockClass;
  if (book.stock === 0) {
    stockLabel = 'Out of stock'; stockClass = 'no-stock';
  } else if (book.stock <= 5) {
    stockLabel = `Only ${book.stock} left!`; stockClass = 'low-stock';
  } else {
    stockLabel = `${book.stock} in stock`; stockClass = 'in-stock';
  }

  const isAddDisabled = !isInWishlist && state.isOverLimit;

  const card = document.createElement('div');
  card.className = 'book-card';
  card.dataset.bookId = book.id;
  card.setAttribute('title', book.title);

  card.innerHTML = `
    <div class="book-spine ${spineClass(book.category)}"></div>
    <div class="book-content">
      <div class="book-category-icon">${icon}</div>
      <div class="book-title">${escapeHtml(book.title)}</div>
      <div class="book-author">
        <svg width="11" height="11" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <path d="M12 2 C14 4 14 7 10 10 L6 14 L2 14 L2 10 L6 6 Z" stroke="currentColor" stroke-width="1.3" fill="none"/>
        </svg>
        ${escapeHtml(book.author)}
      </div>
      <div class="book-price">₹${book.price.toFixed(2)}</div>
      <div class="book-stock ${stockClass}">
        <svg width="11" height="11" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <rect x="2" y="11" width="12" height="3" rx="1" fill="currentColor" opacity="0.7"/>
          <rect x="3" y="7"  width="10" height="3" rx="1" fill="currentColor" opacity="0.5"/>
          <rect x="4" y="3"  width="8"  height="3" rx="1" fill="currentColor" opacity="0.3"/>
        </svg>
        ${stockLabel}
      </div>
    </div>
    <button
      class="card-wishlist-btn ${isInWishlist ? 'in-wishlist' : 'not-in-wishlist'} ${isAddDisabled ? 'btn-disabled' : ''}"
      data-book-id="${book.id}"
      ${isAddDisabled ? 'disabled' : ''}
      title="${isInWishlist ? 'Remove from wishlist' : (isAddDisabled ? 'Wishlist has over 10 books. Remove books to add new ones.' : 'Add to wishlist')}"
      aria-label="${isInWishlist ? 'Remove from wishlist' : 'Add to wishlist'}"
      onclick="handleWishlistToggle(event, ${book.id})"
    >${isInWishlist ? '♥' : '♡'}</button>
  `;

  // Click on the card body opens the modal
  card.addEventListener('click', (e) => {
    if (e.target.closest('.card-wishlist-btn')) return; // ignore heart click
    openModal(book);
  });

  return card;
}

// ═══════════════════════════════════════════════════════════════════════════
//  SEARCH / FILTER / SORT
// ═══════════════════════════════════════════════════════════════════════════

// Debounce timer — waits 350ms after the user stops typing before fetching
let searchTimer;
function debounceSearch() {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => applyFilters(), 350);
}

function applyFilters() {
  const search   = document.getElementById('searchInput').value.trim();
  const category = document.getElementById('categoryFilter').value;
  const sort     = document.getElementById('sortSelect').value;
  loadBooks(search, category, sort);
}

// ═══════════════════════════════════════════════════════════════════════════
//  USER SWITCHER
// ═══════════════════════════════════════════════════════════════════════════
async function switchUser(userId) {
  if (!userId) return;
  state.currentUserId = Number(userId);
  await refreshWishlist();      // loads wishlist for the new user
  applyFilters();               // re-renders books so heart icons update
}

// ═══════════════════════════════════════════════════════════════════════════
//  WISHLIST OPERATIONS
// ═══════════════════════════════════════════════════════════════════════════

/** Load wishlist from server and update all UI pieces. */
async function refreshWishlist() {
  if (!state.currentUserId) return;
  try {
    const data = await apiFetch(`/api/wishlist/${state.currentUserId}`);
    updateWishlistState(data);
  } catch (e) {
    console.error('Failed to load wishlist', e);
  }
}

/** Update all UI that depends on wishlist data. */
function updateWishlistState(data) {
  // data = { books: [...], count: n, limit: 10, over_limit: bool }
  state.wishlistBookIds = new Set(data.books.map(b => b.id));
  state.isOverLimit     = data.over_limit;

  // ── Badge in navbar ──
  document.getElementById('wishlistBadge').textContent = data.count;

  // ── Over-limit banners ──
  const banner1 = document.getElementById('overLimitBanner');
  const banner2 = document.getElementById('wishlistOverLimitBanner');
  if (data.over_limit) {
    banner1.classList.remove('hidden');
    banner2.classList.remove('hidden');
  } else {
    banner1.classList.add('hidden');
    banner2.classList.add('hidden');
  }

  // ── Progress bar ──
  const fill    = document.getElementById('progressFill');
  const counter = document.getElementById('wishlistCounter');
  const pct     = Math.min((data.count / data.limit) * 100, 100);
  fill.style.width = pct + '%';
  counter.textContent = `${data.count} / ${data.limit}`;
  if (data.count >= data.limit) fill.classList.add('full');
  else                           fill.classList.remove('full');

  // ── Render wishlist page grid ──
  renderWishlistGrid(data.books);

  // ── Update heart icons in catalog grid ──
  document.querySelectorAll('.card-wishlist-btn').forEach(btn => {
    const bid = Number(btn.dataset.bookId);
    const inW = state.wishlistBookIds.has(bid);
    btn.className = `card-wishlist-btn ${inW ? 'in-wishlist' : 'not-in-wishlist'}`;
    btn.textContent = inW ? '♥' : '♡';

    if (!inW && state.isOverLimit) {
      btn.disabled = true;
      btn.classList.add('btn-disabled');
      btn.title = 'Wishlist has over 10 books. Remove books to add new ones.';
    } else {
      btn.disabled = false;
      btn.classList.remove('btn-disabled');
      btn.title = inW ? 'Remove from wishlist' : 'Add to wishlist';
    }
  });
}

/** Handle the heart click on a book card. */
async function handleWishlistToggle(event, bookId) {
  event.stopPropagation();
  if (!state.currentUserId) {
    showToast('Please select a user first!', 'info');
    return;
  }

  const btn = event.currentTarget;

  // Animate the heart
  btn.classList.add('heart-pop');
  btn.classList.add('btn-bounce');
  setTimeout(() => { btn.classList.remove('heart-pop'); btn.classList.remove('btn-bounce'); }, 500);

  const isInWishlist = state.wishlistBookIds.has(bookId);

  if (isInWishlist) {
    await removeFromWishlist(bookId);
  } else {
    await addToWishlist(bookId);
  }
}

async function addToWishlist(bookId) {
  // Block if over limit
  if (state.isOverLimit || state.wishlistBookIds.size >= 10) {
    document.getElementById('limitModal').classList.remove('hidden');
    return;
  }

  try {
    const data = await apiFetch(`/api/wishlist/${state.currentUserId}/${bookId}`, 'POST');
    updateWishlistState(data);
    showToast('Added to wishlist ♥', 'success');

    // Update modal button if the same book is open
    if (state.currentBook && state.currentBook.id === bookId) {
      updateModalWishlistBtn(true);
    }
  } catch (e) {
    if (e.status === 409) {
      if (e.message && e.message.toLowerCase().includes('limit')) {
        document.getElementById('limitModal').classList.remove('hidden');
      } else {
        showToast('Already in your wishlist!', 'info');
      }
    } else {
      showToast(e.message || 'Could not add to wishlist', 'error');
    }
  }
}

async function removeFromWishlist(bookId) {
  try {
    const data = await apiFetch(`/api/wishlist/${state.currentUserId}/${bookId}`, 'DELETE');
    updateWishlistState(data);
    showToast('Removed from wishlist', 'info');

    if (state.currentBook && state.currentBook.id === bookId) {
      updateModalWishlistBtn(false);
    }
  } catch (e) {
    showToast(e.message || 'Could not remove from wishlist', 'error');
  }
}

/** Render the wishlist page grid. */
function renderWishlistGrid(books) {
  const grid  = document.getElementById('wishlistGrid');
  const empty = document.getElementById('emptyWishlist');

  // Clear old cards
  Array.from(grid.children).forEach(c => {
    if (c.id !== 'emptyWishlist') grid.removeChild(c);
  });

  if (books.length === 0) {
    empty.classList.remove('hidden');
    return;
  }
  empty.classList.add('hidden');

  books.forEach(book => {
    const icon = CATEGORY_ICONS[book.category] || '📚';
    const card = document.createElement('div');
    card.className = 'book-card';
    card.style.flexDirection = 'column';

    card.innerHTML = `
      <div style="display:flex;flex:1">
        <div class="book-spine ${spineClass(book.category)}"></div>
        <div class="book-content">
          <div class="book-category-icon">${icon}</div>
          <div class="book-title">${escapeHtml(book.title)}</div>
          <div class="book-author">${escapeHtml(book.author)}</div>
          <div class="book-price">₹${book.price.toFixed(2)}</div>
        </div>
      </div>
      <button class="wishlist-remove-btn" onclick="removeFromWishlist(${book.id})">
        🗑 Remove
      </button>
    `;
    grid.appendChild(card);
  });
}

// ═══════════════════════════════════════════════════════════════════════════
//  BOOK DETAIL MODAL
// ═══════════════════════════════════════════════════════════════════════════
function openModal(book) {
  state.currentBook = book;
  const isInWishlist = state.wishlistBookIds.has(book.id);

  // Stock label
  let stockText;
  if (book.stock === 0)      stockText = 'Out of stock';
  else if (book.stock <= 5)  stockText = `Only ${book.stock} left!`;
  else                       stockText = `${book.stock} in stock`;

  document.getElementById('modalTitle').textContent   = book.title;
  document.getElementById('modalAuthorText').textContent = book.author;
  // Update the price and stock text spans (after the SVG icon)
  const priceEl = document.getElementById('modalPrice');
  priceEl.querySelector('.modal-price-text').textContent = ` ₹${book.price.toFixed(2)}`;
  const stockEl = document.getElementById('modalStock');
  stockEl.querySelector('.modal-stock-text').textContent = ` ${stockText}`;
  document.getElementById('modalSpine').className = `modal-spine ${spineClass(book.category)}`;

  const catBadge = document.getElementById('modalCategory');
  catBadge.textContent  = `${CATEGORY_ICONS[book.category] || '📚'} ${book.category}`;
  catBadge.className    = `modal-category-badge ${badgeClass(book.category)}`;

  updateModalWishlistBtn(isInWishlist);

  document.getElementById('modalOverlay').classList.remove('hidden');
  document.body.style.overflow = 'hidden'; // prevent background scroll
}

function updateModalWishlistBtn(isInWishlist) {
  const btn = document.getElementById('modalWishlistBtn');
  if (isInWishlist) {
    btn.disabled = false;
    btn.textContent = '✓ In Wishlist (click to remove)';
    btn.className   = 'btn-primary modal-wishlist-btn in-wishlist';
  } else if (state.isOverLimit) {
    btn.disabled = true;
    btn.textContent = '⚠️ Over Limit (Cannot Add)';
    btn.className   = 'btn-primary modal-wishlist-btn btn-disabled';
  } else {
    btn.disabled = false;
    btn.textContent = '♥ Add to Wishlist';
    btn.className   = 'btn-primary modal-wishlist-btn';
  }
}

function toggleWishlistFromModal() {
  if (!state.currentBook) return;
  const bookId = state.currentBook.id;
  if (state.wishlistBookIds.has(bookId)) {
    removeFromWishlist(bookId);
  } else {
    addToWishlist(bookId);
  }
}

function closeModal(event) {
  // If called with an event (overlay click), only close if clicking the backdrop itself
  if (event && event.target !== document.getElementById('modalOverlay')) return;
  document.getElementById('modalOverlay').classList.add('hidden');
  document.body.style.overflow = '';
  state.currentBook = null;
}

// Allow close via keyboard Escape key
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    document.getElementById('modalOverlay').classList.add('hidden');
    document.getElementById('limitModal').classList.add('hidden');
    const formModal = document.getElementById('bookFormModal');
    if (formModal) formModal.classList.add('hidden');
    document.body.style.overflow = '';
  }
});

// ═══════════════════════════════════════════════════════════════════════════
//  LIMIT MODAL
// ═══════════════════════════════════════════════════════════════════════════
function closeLimitModal(event) {
  if (event && event.target !== document.getElementById('limitModal')) return;
  document.getElementById('limitModal').classList.add('hidden');
}

// ═══════════════════════════════════════════════════════════════════════════
//  PAGE NAVIGATION
// ═══════════════════════════════════════════════════════════════════════════
function showPage(page) {
  document.querySelectorAll('.page').forEach(p => {
    p.classList.remove('active');
    p.classList.add('hidden');
  });
  document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));

  const target = document.getElementById(page + 'Page');
  target.classList.remove('hidden');
  target.classList.add('active');

  const tab = document.querySelector(`[data-page="${page}"]`);
  if (tab) tab.classList.add('active');

  // Trigger page-specific refreshes
  if (page === 'wishlist') refreshWishlist();
  if (page === 'admin')    loadAdminInventory();
}

// ═══════════════════════════════════════════════════════════════════════════
//  ADMIN INVENTORY & MANAGEMENT
// ═══════════════════════════════════════════════════════════════════════════
let adminBooksCache = [];

async function loadAdminInventory() {
  try {
    const books = await apiFetch('/api/books');
    adminBooksCache = books;
    updateAdminStats(books);
    populateAdminCategories(books);
    renderAdminTable(books);
  } catch (e) {
    showToast('Failed to load inventory for admin', 'error');
  }
}

function updateAdminStats(books) {
  const total = books.length;
  const categories = new Set(books.map(b => b.category)).size;
  const lowStock = books.filter(b => b.stock > 0 && b.stock <= 5).length;
  const outStock = books.filter(b => b.stock === 0).length;

  const tEl = document.getElementById('statTotalBooks');
  const cEl = document.getElementById('statCategories');
  const lEl = document.getElementById('statLowStock');
  const oEl = document.getElementById('statOutStock');

  if (tEl) tEl.textContent = total;
  if (cEl) cEl.textContent = categories;
  if (lEl) lEl.textContent = lowStock;
  if (oEl) oEl.textContent = outStock;
}

function populateAdminCategories(books) {
  const sel = document.getElementById('adminCategorySelect');
  if (!sel) return;
  const currentVal = sel.value;
  sel.innerHTML = '<option value="">All Categories</option>';
  const cats = Array.from(new Set(books.map(b => b.category))).sort();
  cats.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c;
    opt.textContent = `${CATEGORY_ICONS[c] || '📚'} ${c}`;
    sel.appendChild(opt);
  });
  if (cats.includes(currentVal)) sel.value = currentVal;
}

function renderAdminTable(books) {
  const tbody = document.getElementById('adminTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  if (books.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2.5rem;">No books found matching criteria.</td></tr>`;
    return;
  }

  books.forEach(book => {
    const tr = document.createElement('tr');

    let stockBadgeClass = 'in-stock';
    let stockText = `${book.stock} in stock`;
    if (book.stock === 0) {
      stockBadgeClass = 'no-stock';
      stockText = 'Out of stock (0)';
    } else if (book.stock <= 5) {
      stockBadgeClass = 'low-stock';
      stockText = `Low (${book.stock} left)`;
    }

    const icon = CATEGORY_ICONS[book.category] || '📚';
    const escapedTitle = escapeHtml(book.title);
    const safeTitleArg = escapedTitle.replace(/'/g, "\\'");

    tr.innerHTML = `
      <td>
        <div class="admin-spine-preview ${spineClass(book.category)}"></div>
      </td>
      <td>
        <div class="admin-book-title">${escapedTitle}</div>
        <div style="font-size: 0.72rem; color: var(--text-muted);">ID: #${book.id}</div>
      </td>
      <td>
        <div class="admin-book-author">✍️ ${escapeHtml(book.author)}</div>
      </td>
      <td>
        <span class="modal-category-badge ${badgeClass(book.category)}" style="font-size: 0.7rem; padding: 0.15rem 0.5rem; margin: 0;">
          ${icon} ${escapeHtml(book.category)}
        </span>
      </td>
      <td style="font-weight: 600; color: #c8700a;">
        ₹${book.price.toFixed(2)}
      </td>
      <td>
        <span class="book-stock ${stockBadgeClass}" style="font-weight: 500;">
          ${stockText}
        </span>
      </td>
      <td style="text-align: right;">
        <div class="btn-action-group">
          <button class="btn-table-edit" onclick="openEditBookModal(${book.id})" title="Edit book details">
            ✏️ Edit
          </button>
          <button class="btn-table-delete" onclick="handleDeleteBook(${book.id}, '${safeTitleArg}')" title="Delete book">
            🗑️
          </button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function filterAdminTable() {
  const query = (document.getElementById('adminSearchInput')?.value || '').toLowerCase().trim();
  const category = document.getElementById('adminCategorySelect')?.value || '';

  const filtered = adminBooksCache.filter(b => {
    const matchText = !query || b.title.toLowerCase().includes(query) || b.author.toLowerCase().includes(query);
    const matchCat = !category || b.category.toLowerCase() === category.toLowerCase();
    return matchText && matchCat;
  });

  renderAdminTable(filtered);
}

function openAddBookModal() {
  document.getElementById('bookForm').reset();
  document.getElementById('formBookId').value = '';
  document.getElementById('bookFormTitle').textContent = '➕ Add New Book';
  document.getElementById('formSubmitBtn').innerHTML = '<span>💾</span> Publish Book';
  document.getElementById('formErrorMsg').classList.add('hidden');
  document.getElementById('bookFormModal').classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function openEditBookModal(bookId) {
  const book = adminBooksCache.find(b => b.id === bookId);
  if (!book) return;

  document.getElementById('formBookId').value = book.id;
  document.getElementById('formTitle').value = book.title;
  document.getElementById('formAuthor').value = book.author;
  document.getElementById('formCategory').value = book.category;
  document.getElementById('formPrice').value = book.price;
  document.getElementById('formStock').value = book.stock;

  document.getElementById('bookFormTitle').textContent = `✏️ Edit "${book.title}"`;
  document.getElementById('formSubmitBtn').innerHTML = '<span>💾</span> Update Book';
  document.getElementById('formErrorMsg').classList.add('hidden');
  document.getElementById('bookFormModal').classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function closeBookFormModal(event) {
  if (event && event.target !== document.getElementById('bookFormModal')) return;
  document.getElementById('bookFormModal').classList.add('hidden');
  document.body.style.overflow = '';
}

async function handleBookFormSubmit(event) {
  event.preventDefault();

  const idVal = document.getElementById('formBookId').value;
  const title = document.getElementById('formTitle').value.trim();
  const author = document.getElementById('formAuthor').value.trim();
  const category = document.getElementById('formCategory').value.trim();
  const price = parseFloat(document.getElementById('formPrice').value);
  const stock = parseInt(document.getElementById('formStock').value, 10);

  const errorEl = document.getElementById('formErrorMsg');

  if (!title || !author || !category) {
    errorEl.textContent = 'Please fill in all required fields (title, author, category).';
    errorEl.classList.remove('hidden');
    return;
  }

  if (isNaN(price) || price <= 0) {
    errorEl.textContent = 'Price must be greater than 0.';
    errorEl.classList.remove('hidden');
    return;
  }

  if (isNaN(stock) || stock < 0) {
    errorEl.textContent = 'Stock cannot be negative.';
    errorEl.classList.remove('hidden');
    return;
  }

  errorEl.classList.add('hidden');

  const payload = { title, author, category, price, stock };

  try {
    if (idVal) {
      await apiFetch(`/api/books/${idVal}`, 'PUT', payload);
      showToast(`Updated "${title}" successfully! ✨`, 'success');
    } else {
      await apiFetch('/api/books', 'POST', payload);
      showToast(`Added "${title}" to catalog! 📚`, 'success');
    }

    closeBookFormModal();
    await loadAdminInventory();
    await loadCategories();
    applyFilters();
  } catch (e) {
    errorEl.textContent = e.message || 'Failed to save book.';
    errorEl.classList.remove('hidden');
  }
}

async function handleDeleteBook(bookId, title) {
  if (!confirm(`Are you sure you want to delete "${title}" from the catalog?`)) {
    return;
  }

  try {
    await apiFetch(`/api/books/${bookId}`, 'DELETE');
    showToast(`Deleted "${title}"`, 'info');
    await loadAdminInventory();
    await loadCategories();
    applyFilters();
    refreshWishlist();
  } catch (e) {
    showToast(e.message || 'Failed to delete book', 'error');
  }
}

// ═══════════════════════════════════════════════════════════════════════════
//  DARK / LIGHT MODE TOGGLE
// ═══════════════════════════════════════════════════════════════════════════
function toggleTheme() {
  const html = document.documentElement;
  const isDark = html.getAttribute('data-theme') === 'dark';
  html.setAttribute('data-theme', isDark ? 'light' : 'dark');
  document.getElementById('themeIcon').textContent = isDark ? '🌙' : '☀️';
  localStorage.setItem('theme', isDark ? 'light' : 'dark');
}

// Restore saved theme preference on load
(function restoreTheme() {
  const urlTheme = new URLSearchParams(window.location.search).get('theme');
  const saved = urlTheme || localStorage.getItem('theme');
  if (saved) {
    document.documentElement.setAttribute('data-theme', saved);
    const icon = document.getElementById('themeIcon');
    if (icon) icon.textContent = saved === 'dark' ? '☀️' : '🌙';
  }
})();

// ═══════════════════════════════════════════════════════════════════════════
//  LOADING STATE
// ═══════════════════════════════════════════════════════════════════════════
function showLoading(show) {
  const el = document.getElementById('loadingState');
  if (el) el.style.display = show ? 'flex' : 'none';
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOAST NOTIFICATIONS
// ═══════════════════════════════════════════════════════════════════════════
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icon = { success: '✅', error: '❌', info: 'ℹ️' }[type] || 'ℹ️';
  toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;

  container.appendChild(toast);

  // Remove after 3 seconds
  setTimeout(() => {
    toast.remove();
  }, 3000);
}

// ═══════════════════════════════════════════════════════════════════════════
//  API HELPER
// ═══════════════════════════════════════════════════════════════════════════

/**
 * apiFetch — a small wrapper around the browser's fetch() API.
 * When running with the Spring Boot backend, it calls the Java REST API.
 * When deployed as a static site (e.g. GitHub Pages), it smoothly falls back
 * to a client-side mock store with the exact same business rules and data!
 */
const IS_STATIC_HOST = window.location.hostname.includes('github.io') || window.location.protocol === 'file:';

async function apiFetch(url, method = 'GET', body = null) {
  if (IS_STATIC_HOST) {
    return mockApiHandler(url, method, body);
  }

  try {
    const opts = {
      method,
      headers: { 'Content-Type': 'application/json' },
    };
    if (body) opts.body = JSON.stringify(body);

    const response = await fetch(url, opts);

    if (!response.ok) {
      let errData;
      try { errData = await response.json(); } catch (_) { errData = {}; }
      const err = new Error(errData.error || `HTTP ${response.status}`);
      err.status = response.status;
      err.message = errData.error || `HTTP ${response.status}`;
      throw err;
    }

    if (response.status === 204) return null;
    return response.json();
  } catch (err) {
    // If backend is unreachable on custom domain, fallback to mock store
    if (err.name === 'TypeError' || err.status === 404) {
      console.warn('Backend API not reachable. Using static demo store.', err);
      return mockApiHandler(url, method, body);
    }
    throw err;
  }
}

// ─── Static Demo Store for GitHub Pages ─────────────────────────────────────
const MOCK_STORE_KEY = 'booknook_mock_db_v1';
function getMockDB() {
  const saved = localStorage.getItem(MOCK_STORE_KEY);
  if (saved) {
    try { return JSON.parse(saved); } catch (_) {}
  }
  const initial = {
    users: [
      { id: 1, name: "Priya Sharma" },
      { id: 2, name: "Arjun Mehta" },
      { id: 3, name: "Meera Nair" }
    ],
    wishlists: {
      1: [],
      2: [1, 2, 3, 4, 5, 6, 7, 8, 9],
      3: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
    },
    books: [
      { id:1, title:"The Midnight Library", author:"Matt Haig", category:"Fiction", price:499.00, stock:45 },
      { id:2, title:"The Alchemist", author:"Paulo Coelho", category:"Fiction", price:299.00, stock:80 },
      { id:3, title:"Ikigai", author:"Héctor García", category:"Fiction", price:349.00, stock:60 },
      { id:4, title:"Where the Crawdads Sing", author:"Delia Owens", category:"Fiction", price:599.00, stock:30 },
      { id:5, title:"The Kite Runner", author:"Khaled Hosseini", category:"Fiction", price:399.00, stock:8 },
      { id:6, title:"A Brief History of Time", author:"Stephen Hawking", category:"Science", price:549.00, stock:25 },
      { id:7, title:"The Gene: An Intimate History", author:"Siddhartha Mukherjee", category:"Science", price:799.00, stock:15 },
      { id:8, title:"Cosmos", author:"Carl Sagan", category:"Science", price:649.00, stock:3 },
      { id:9, title:"Sapiens", author:"Yuval Noah Harari", category:"History", price:699.00, stock:50 },
      { id:10, title:"India After Gandhi", author:"Ramachandra Guha", category:"History", price:899.00, stock:20 },
      { id:11, title:"The Discovery of India", author:"Jawaharlal Nehru", category:"History", price:449.00, stock:35 },
      { id:12, title:"Clean Code", author:"Robert C. Martin", category:"Tech", price:749.00, stock:40 },
      { id:13, title:"The Pragmatic Programmer", author:"David Thomas", category:"Tech", price:849.00, stock:22 },
      { id:14, title:"Design Patterns", author:"Gang of Four", category:"Tech", price:999.00, stock:18 },
      { id:15, title:"You Don't Know JS", author:"Kyle Simpson", category:"Tech", price:399.00, stock:0 },
      { id:16, title:"Harry Potter & the Sorcerer's Stone", author:"J.K. Rowling", category:"Kids", price:449.00, stock:70 },
      { id:17, title:"The Little Prince", author:"Antoine de Saint-Exupéry", category:"Kids", price:249.00, stock:55 },
      { id:18, title:"Matilda", author:"Roald Dahl", category:"Kids", price:299.00, stock:40 },
      { id:19, title:"Atomic Habits", author:"James Clear", category:"Self-Help", price:599.00, stock:90 },
      { id:20, title:"Deep Work", author:"Cal Newport", category:"Self-Help", price:499.00, stock:35 }
    ]
  };
  saveMockDB(initial);
  return initial;
}

function saveMockDB(db) {
  localStorage.setItem(MOCK_STORE_KEY, JSON.stringify(db));
}

async function mockApiHandler(url, method, body) {
  const db = getMockDB();
  const parsed = new URL(url, 'http://localhost');
  const path = parsed.pathname;

  // GET /api/users
  if (path === '/api/users' && method === 'GET') {
    return db.users;
  }

  // GET /api/categories
  if (path === '/api/categories' && method === 'GET') {
    const cats = Array.from(new Set(db.books.map(b => b.category))).sort();
    return cats;
  }

  // GET /api/books
  if (path === '/api/books' && method === 'GET') {
    let result = [...db.books];
    const search = parsed.searchParams.get('search');
    const category = parsed.searchParams.get('category');
    const sort = parsed.searchParams.get('sort');

    if (search) {
      const q = search.toLowerCase();
      result = result.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q));
    }
    if (category) {
      result = result.filter(b => b.category.toLowerCase() === category.toLowerCase());
    }
    if (sort === 'price_asc') {
      result.sort((a,b) => a.price - b.price);
    } else if (sort === 'price_desc') {
      result.sort((a,b) => b.price - a.price);
    } else {
      result.sort((a,b) => a.title.localeCompare(b.title));
    }
    return result;
  }

  // POST /api/books (Admin Add)
  if (path === '/api/books' && method === 'POST') {
    const newId = db.books.length > 0 ? Math.max(...db.books.map(b => b.id)) + 1 : 1;
    const newBook = { id: newId, ...body };
    db.books.push(newBook);
    saveMockDB(db);
    return newBook;
  }

  // PUT /api/books/:id (Admin Edit)
  const putMatch = path.match(/^\/api\/books\/(\d+)$/);
  if (putMatch && method === 'PUT') {
    const bookId = Number(putMatch[1]);
    const idx = db.books.findIndex(b => b.id === bookId);
    if (idx === -1) {
      const err = new Error(`Book not found with id: ${bookId}`);
      err.status = 404; throw err;
    }
    db.books[idx] = { id: bookId, ...body };
    saveMockDB(db);
    return db.books[idx];
  }

  // DELETE /api/books/:id (Admin Delete)
  const delMatch = path.match(/^\/api\/books\/(\d+)$/);
  if (delMatch && method === 'DELETE') {
    const bookId = Number(delMatch[1]);
    db.books = db.books.filter(b => b.id !== bookId);
    // Also remove from any wishlists
    for (const uid in db.wishlists) {
      db.wishlists[uid] = db.wishlists[uid].filter(id => id !== bookId);
    }
    saveMockDB(db);
    return null;
  }

  // GET /api/wishlist/:userId
  const wishGetMatch = path.match(/^\/api\/wishlist\/(\d+)$/);
  if (wishGetMatch && method === 'GET') {
    const uid = Number(wishGetMatch[1]);
    const ids = db.wishlists[uid] || [];
    const books = ids.map(id => db.books.find(b => b.id === id)).filter(Boolean);
    return {
      books,
      count: books.length,
      limit: 10,
      over_limit: books.length > 10
    };
  }

  // POST /api/wishlist/:userId/:bookId
  const wishAddMatch = path.match(/^\/api\/wishlist\/(\d+)\/(\d+)$/);
  if (wishAddMatch && method === 'POST') {
    const uid = Number(wishAddMatch[1]);
    const bid = Number(wishAddMatch[2]);
    if (!db.wishlists[uid]) db.wishlists[uid] = [];
    const list = db.wishlists[uid];

    if (list.includes(bid)) {
      const err = new Error(`Book ${bid} is already in the wishlist of user ${uid}.`);
      err.status = 409; throw err;
    }
    if (list.length >= 10) {
      const err = new Error('Wishlist limit reached. Remove a book to add a new one.');
      err.status = 409; throw err;
    }

    list.push(bid);
    saveMockDB(db);
    const books = list.map(id => db.books.find(b => b.id === id)).filter(Boolean);
    return {
      books,
      count: books.length,
      limit: 10,
      over_limit: books.length > 10
    };
  }

  // DELETE /api/wishlist/:userId/:bookId
  const wishDelMatch = path.match(/^\/api\/wishlist\/(\d+)\/(\d+)$/);
  if (wishDelMatch && method === 'DELETE') {
    const uid = Number(wishDelMatch[1]);
    const bid = Number(wishDelMatch[2]);
    if (db.wishlists[uid]) {
      db.wishlists[uid] = db.wishlists[uid].filter(id => id !== bid);
      saveMockDB(db);
    }
    const list = db.wishlists[uid] || [];
    const books = list.map(id => db.books.find(b => b.id === id)).filter(Boolean);
    return {
      books,
      count: books.length,
      limit: 10,
      over_limit: books.length > 10
    };
  }

  throw new Error(`Endpoint not found: ${path}`);
}

// ═══════════════════════════════════════════════════════════════════════════
//  UTILITY
// ═══════════════════════════════════════════════════════════════════════════

/** Escape HTML special characters to prevent XSS */
function escapeHtml(text) {
  const map = { '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#039;' };
  return String(text).replace(/[&<>"']/g, m => map[m]);
}
