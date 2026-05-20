# SmartExpense: Ke hoach phat trien tinh nang thuc dung

Tai lieu nay dinh huong phat trien SmartExpense theo hai tieu chi chinh:

1. **Tinh ung dung cao**: uu tien cac luong nguoi dung can hang ngay: ghi chi tieu nhanh, xem so du, kiem soat ngan sach, bao cao ro rang.
2. **Khong lan man mau me**: giao dien gon, it hieu ung, it man hinh thua; moi tinh nang phai giai quyet mot van de cu the.

---

## 1. Hien trang du an

### Backend

- Thu muc: `BE_SmartExpense/smart_expense`
- Cong nghe: Java 21, Spring Boot, Spring MVC, Spring JDBC, MySQL, Lombok.
- Module da co: user, wallet, category, transaction, budget, notification, analytics, savings goal, recurring transaction.

### Android app

- Thu muc: `SmartExpense`
- Cong nghe: Android Java, XML layout, Material Components, Retrofit/Gson, MPAndroidChart.
- Man hinh da co: splash, login, register, dashboard, transaction, add transaction, analytics, profile.

Ket luan: du an da co nen tang kha day du. Giai doan tiep theo nen tap trung hoan thien luong chinh, on dinh du lieu, va lam cac canh bao thuc su huu ich thay vi mo rong qua nhieu tinh nang cung luc.

---

## 2. Nguyen tac san pham

### Nen lam

- Nhap giao dich trong toi da 10-15 giay.
- Dashboard tra loi nhanh 3 cau hoi: con bao nhieu tien, thang nay da chi bao nhieu, danh muc nao dang ton tien nhat.
- Bao cao du de ra quyet dinh, khong can qua nhieu bieu do.
- Canh bao ngan sach dua tren nguong ro rang, de hieu.
- Moi tinh nang co trang thai rong, loading, loi mang, va thong bao sau khi luu.

### Khong nen lam trong giai doan dau

- Khong them AI/ML neu rule-based da du.
- Khong lam qua nhieu bieu do, animation, mau sac trang tri.
- Khong them social, gamification, diem thuong.
- Khong them qua nhieu loai tai san phuc tap nhu chung khoan, crypto, dau tu.
- Khong phat trien dong thoi qua nhieu module lon neu giao dich/vi/bao cao chua chac.

---

## 3. MVP uu tien cao

### 3.1. Tai khoan va phien dang nhap

Muc tieu: nguoi dung dang ky, dang nhap va tiep tuc dung app ma khong bi mat phien lien tuc.

Tinh nang:

- Dang ky bang ten, email, mat khau.
- Dang nhap bang email/mat khau.
- Luu userId hoac token phien trong local storage cua app.
- Dang xuat o man hinh profile.
- Kiem tra loi co ban: email trung, sai mat khau, bo trong truong.

Tieu chi hoan thanh:

- Login thanh cong thi vao dashboard.
- Dong/mo app van giu trang thai dang nhap neu phien con hop le.
- Loi API hien thi bang thong bao ngan gon, khong crash app.

### 3.2. Vi tien

Muc tieu: nguoi dung biet minh dang co bao nhieu tien theo tung nguon.

Tinh nang:

- Tao vi: tien mat, ngan hang, vi dien tu.
- Xem danh sach vi va tong so du.
- Sua ten vi, loai vi, so du ban dau.
- Xoa vi chi khi khong co giao dich hoac canh bao ro anh huong.
- Tu dong cap nhat so du khi them giao dich thu/chi.

Tieu chi hoan thanh:

- Them giao dich chi thi so du vi giam.
- Them giao dich thu thi so du vi tang.
- Dashboard hien tong so du tat ca vi.

### 3.3. Giao dich thu/chi

Muc tieu: day la tinh nang cot loi, can nhanh va it thao tac.

Tinh nang:

- Them giao dich gom: loai giao dich, so tien, vi, danh muc, ngay, ghi chu.
- Danh muc mac dinh: an uong, di chuyen, nha cua, hoa don, mua sam, giai tri, suc khoe, luong, thu khac.
- Danh sach giao dich theo ngay gan nhat.
- Loc theo khoang ngay, vi, danh muc, loai thu/chi.
- Sua/xoa giao dich va cap nhat lai so du vi.

Tieu chi hoan thanh:

- Nguoi dung them giao dich moi trong mot man hinh.
- Danh sach cap nhat ngay sau khi them/sua/xoa.
- Khong cho nhap so tien <= 0.
- Neu mat mang, app bao loi ro va khong lam sai so du hien thi.

### 3.4. Dashboard thuc dung

Muc tieu: vao app la nam duoc tinh hinh tai chinh trong thang.

Thanh phan nen co:

- Tong so du hien tai.
- Tong thu, tong chi trong thang.
- Chenh lech thu - chi.
- 5 giao dich gan nhat.
- 3 danh muc chi nhieu nhat trong thang.
- Canh bao ngan sach neu co.

Khong nen co:

- Hero/illustration lon.
- Nhieu card trang tri khong co du lieu can thiet.
- Bieu do chiem qua nua man hinh neu khong giup ra quyet dinh.

Tieu chi hoan thanh:

- Dashboard tai du lieu nhanh, co loading state.
- Du lieu rong van hien huong dan hanh dong tiep theo: tao vi, them giao dich.

### 3.5. Bao cao co ban

Muc tieu: giup nguoi dung nhin lai thoi quen chi tieu.

Tinh nang:

- Bao cao theo thang.
- Tong thu, tong chi, so tien con lai.
- Bieu do cot thu/chi theo ngay hoac theo tuan.
- Bieu do tron/donut ty le chi theo danh muc.
- Danh sach danh muc chi nhieu nhat.

Tieu chi hoan thanh:

- Chon thang bat ky va xem bao cao.
- So lieu tren bieu do khop voi tong giao dich.
- Khong can bieu do phuc tap hon neu chua co nhu cau that.

### 3.6. Ngan sach va canh bao

Muc tieu: giup nguoi dung khong chi qua muc da dat.

Tinh nang:

- Tao ngan sach theo danh muc va theo thang.
- Hien da chi / gioi han / con lai.
- Canh bao khi dat 80%, 100% ngan sach.
- Dashboard hien cac ngan sach dang gan vuot.

Rule de dung:

- Duoi 80%: binh thuong.
- Tu 80% den duoi 100%: canh bao vang.
- Tu 100% tro len: canh bao do.

Tieu chi hoan thanh:

- Khi them giao dich moi, ngan sach lien quan cap nhat ngay.
- Canh bao ngan sach khong spam; moi nguong chi nen tao thong bao mot lan trong chu ky.

---

## 4. Tinh nang sau MVP

Chi nen lam sau khi cac tinh nang MVP chay on dinh.

### 4.1. Giao dich dinh ky

Ung dung cao cho: tien nha, dien nuoc, internet, hoc phi, luong hang thang.

Pham vi nen lam:

- Tao giao dich lap lai theo thang/tuan/ngay.
- Den ngay thi tao giao dich hoac nhac nguoi dung xac nhan.
- Co trang thai bat/tat.

### 4.2. Muc tieu tiet kiem

Ung dung cao cho: mua laptop, du lich, quy khan cap.

Pham vi nen lam:

- Tao muc tieu: ten, so tien muc tieu, han hoan thanh.
- Them tien vao muc tieu.
- Hien tien do phan tram.
- Goi y so tien can tiet kiem moi thang.

### 4.3. Phat hien bat thuong don gian

Chi nen giu o muc rule-based.

Rule de trien khai:

- Neu giao dich moi lon hon 2 lan trung binh 90 ngay cua cung danh muc thi canh bao.
- Neu danh muc chua co du lieu, khong can canh bao.
- Canh bao chi mang tinh goi y, khong chan nguoi dung luu giao dich.

### 4.4. Xuat du lieu

Ung dung cao cho nguoi dung muon sao luu hoac nop bao cao ca nhan.

Pham vi nen lam:

- Xuat CSV theo khoang ngay.
- Chia cot ro: ngay, vi, danh muc, loai, so tien, ghi chu.

---

## 5. Lo trinh trien khai de xuat

### Tuan 1: Lam chac nen tang du lieu va API

- Kiem tra lai schema MySQL va file khoi tao du lieu.
- Chuan hoa response loi API.
- Hoan thien API user, wallet, category.
- Them validate dau vao o backend.
- Tao du lieu mau de test nhanh tren app.

Ket qua can co:

- Backend chay duoc voi MySQL local.
- Android goi duoc API login/register/wallet/category.

### Tuan 2: Hoan thien giao dich

- API tao/sua/xoa/loc giao dich.
- Cap nhat so du vi theo giao dich.
- Man hinh them giao dich gon, it truong thua.
- Danh sach giao dich co loc co ban.

Ket qua can co:

- Nguoi dung co the quan ly thu/chi hang ngay day du.
- So du vi va danh sach giao dich luon khop.

### Tuan 3: Dashboard va bao cao thang

- API tong hop dashboard.
- API bao cao theo thang.
- Dashboard hien tong so du, thu/chi, giao dich gan nhat.
- Analytics hien bieu do cot/donut don gian.

Ket qua can co:

- Vao app la thay tinh hinh tai chinh trong thang.
- Bao cao giup biet danh muc nao dang chi nhieu.

### Tuan 4: Ngan sach va thong bao

- API ngan sach theo danh muc/thang.
- Logic canh bao 80% va 100%.
- Man hinh ngan sach hoac tich hop vao analytics/dashboard.
- Notification list chi hien canh bao co y nghia.

Ket qua can co:

- Nguoi dung dat ngan sach va duoc canh bao dung luc.
- Khong tao qua nhieu thong bao trung lap.

### Tuan 5: On dinh va danh bong vua du

- Xu ly trang thai rong, loading, loi mang.
- Format tien VND thong nhat.
- Kiem tra cac case sua/xoa giao dich.
- Don lai UI: mau trung tinh, nut ro chuc nang, khoang cach de doc.
- Viet test cho service quan trong o backend.

Ket qua can co:

- App dung on dinh cho luong ca nhan co ban.
- Giao dien sach, de thao tac, khong can trang tri them.

---

## 6. Thu tu uu tien backlog

### P0 - Bat buoc

1. Dang ky, dang nhap, dang xuat.
2. Tao va xem vi.
3. Them/sua/xoa giao dich.
4. Cap nhat so du vi dung.
5. Dashboard tong quan.
6. Bao cao thang co ban.
7. Ngan sach va canh bao 80%/100%.

### P1 - Nen co sau khi P0 on dinh

1. Loc giao dich nang cao hon.
2. Giao dich dinh ky.
3. Muc tieu tiet kiem.
4. Phat hien giao dich bat thuong rule-based.
5. Xuat CSV.

### P2 - De sau

1. Tag giao dich.
2. Quan ly vay/no.
3. Nhap du lieu tu file ngan hang.
4. Da tien te.
5. Dong bo/backup nang cao.

---

## 7. Dinh huong UI/UX

### Mau sac

- Nen dung nen sang hoac toi gian, chu de doc.
- Mot mau chinh cho hanh dong chinh.
- Mau do chi dung cho loi hoac vuot ngan sach.
- Mau xanh chi dung cho thu nhap/thanh cong.
- Khong dung gradient, animation, card long nhau neu khong can.

### Dieu huong

- Bottom navigation chi nen co 4 muc:
  - Tong quan
  - Giao dich
  - Bao cao
  - Ca nhan
- Nut them giao dich nen de thay va dung nhat quan.

### Man hinh

- Moi man hinh nen co mot muc dich chinh.
- Danh sach uu tien kha nang doc va loc nhanh.
- Form nhap lieu can ngan: so tien, loai, vi, danh muc, ngay, ghi chu.

---

## 8. Yeu cau ky thuat nen chuan hoa

### Backend

- Khong tra password hash ve Android.
- Validate du lieu request o controller/service.
- Dung transaction khi tao/sua/xoa giao dich va cap nhat so du vi.
- Response loi nen co format thong nhat: `message`, `code`, `details`.
- Cac query bao cao can filter theo `user_id` de tranh ro ri du lieu.

### Android

- Tach API model, adapter, fragment ro rang.
- Tat ca request mang can co loading va error state.
- Format tien bang VND thong nhat.
- Khong hard-code `userId` trong fragment; lay tu phien dang nhap.
- Khong de app crash khi API tra list rong/null.

---

## 9. Tieu chi nghiem thu ban dau

Ung dung duoc xem la dat muc MVP khi:

1. Nguoi dung moi dang ky, dang nhap, tao vi dau tien thanh cong.
2. Nguoi dung them duoc giao dich thu/chi va so du vi thay doi dung.
3. Dashboard hien dung tong so du, tong thu, tong chi trong thang.
4. Bao cao thang hien dung ty le chi theo danh muc.
5. Nguoi dung tao ngan sach va nhan canh bao khi vuot 80%/100%.
6. App khong crash trong cac tinh huong: mat mang, API loi, du lieu rong, nhap sai form.

---

## 10. Ket luan

Huong phat trien nen la: **giao dich nhanh -> so du dung -> bao cao de hieu -> ngan sach co canh bao**. Khi bon phan nay on dinh, cac tinh nang thong minh nhu giao dich dinh ky, muc tieu tiet kiem va phat hien bat thuong se co gia tri that hon vi duoc xay tren du lieu dung va luong dung ro rang.
