function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

function bookingValue(booking, keys, fallback = '') {
  const key = keys.find((item) => booking?.[item] !== undefined && booking?.[item] !== null && booking?.[item] !== '');
  return key ? booking[key] : fallback;
}

export function downloadBookingReceipt(booking, user) {
  const bookingId = bookingValue(booking, ['bookingId', 'id'], 'N/A');
  const guestName = bookingValue(booking, ['userName', 'guestName'], `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || user?.email || 'Guest');
  const hotelName = bookingValue(booking, ['hotelName'], booking?.hotel?.name || 'Hotel');
  const roomType = bookingValue(booking, ['roomType'], booking?.room?.type || 'Room');
  const checkIn = bookingValue(booking, ['checkInDate', 'checkIn'], 'N/A');
  const checkOut = bookingValue(booking, ['checkOutDate', 'checkOut'], 'N/A');
  const status = bookingValue(booking, ['status'], 'CONFIRMED');
  const amount = Number(bookingValue(booking, ['totalAmount', 'totalPrice', 'amount'], 0)).toLocaleString('en-IN');
  const generatedAt = new Date().toLocaleString('en-IN');

  const html = `<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <title>Booking Receipt #${escapeHtml(bookingId)}</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 0; background: #f4f7fb; color: #172033; }
    .receipt { width: 720px; margin: 40px auto; background: #fff; border: 1px solid #d9e2ef; padding: 32px; }
    .header { display: flex; justify-content: space-between; gap: 24px; border-bottom: 2px solid #172033; padding-bottom: 18px; }
    h1 { margin: 0; font-size: 28px; }
    .muted { color: #64748b; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 24px; }
    .box { border: 1px solid #e2e8f0; padding: 14px; }
    .label { display: block; font-size: 12px; color: #64748b; text-transform: uppercase; margin-bottom: 6px; }
    .value { font-weight: 700; }
    .total { margin-top: 24px; padding: 18px; background: #102033; color: white; display: flex; justify-content: space-between; font-size: 20px; }
    .footer { margin-top: 24px; font-size: 13px; color: #64748b; }
    @media print { body { background: #fff; } .receipt { margin: 0; width: auto; border: 0; } }
  </style>
</head>
<body>
  <main class="receipt">
    <section class="header">
      <div>
        <h1>Booking Receipt</h1>
        <div class="muted">AR Grand Stay</div>
      </div>
      <div>
        <strong>Receipt #${escapeHtml(bookingId)}</strong>
        <div class="muted">${escapeHtml(generatedAt)}</div>
      </div>
    </section>
    <section class="grid">
      <div class="box"><span class="label">Guest</span><span class="value">${escapeHtml(guestName)}</span></div>
      <div class="box"><span class="label">Status</span><span class="value">${escapeHtml(status)}</span></div>
      <div class="box"><span class="label">Hotel</span><span class="value">${escapeHtml(hotelName)}</span></div>
      <div class="box"><span class="label">Room</span><span class="value">${escapeHtml(roomType).replaceAll('_', ' ')}</span></div>
      <div class="box"><span class="label">Check-in</span><span class="value">${escapeHtml(checkIn)}</span></div>
      <div class="box"><span class="label">Check-out</span><span class="value">${escapeHtml(checkOut)}</span></div>
    </section>
    <section class="total">
      <span>Total Paid / Payable</span>
      <strong>Rs. ${escapeHtml(amount)}</strong>
    </section>
    <p class="footer">This receipt was generated from your Hotel Management System booking dashboard.</p>
  </main>
</body>
</html>`;

  const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `booking-receipt-${bookingId}.html`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
