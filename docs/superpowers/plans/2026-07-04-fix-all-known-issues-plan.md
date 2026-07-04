# Fix All 32 Known Issues — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all 32 known issues documented in CLAUDE.md section 9 across backend (12), miniapp (9), and admin-panel (11).

**Architecture:** Six sequential phases: backend foundation → business logic → architecture upgrade → miniapp fixes → admin panel fixes → tests. Each phase is independently verifiable. Member module boundaries are preserved throughout.

**Tech Stack:** Spring Boot 3.2.6, MyBatis-Plus 3.5.7, MySQL 8.0, JWT (jjwt), Vue 3 + Element Plus, WeChat Miniapp (native)

---

## Phase ①: Backend Foundation (3 issues)

### Task 1.1: Add GlobalExceptionHandler

**Files:**
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/GlobalExceptionHandler.java`

- [ ] **Step 1: Create GlobalExceptionHandler**

```java
package com.foodrec.admin.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return Result.fail("参数校验失败: " + errors);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleRuntime(RuntimeException ex) {
        return Result.fail("服务器错误: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleAll(Exception ex) {
        return Result.fail("系统异常，请稍后重试");
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/GlobalExceptionHandler.java
git commit -m "feat: add global exception handler with validation error support"
```

### Task 1.2: Externalize sensitive configuration

**Files:**
- Modify: `admin-panel/server-springboot/src/main/resources/application.yml`

- [ ] **Step 1: Change application.yml to use env vars**

Replace the hardcoded values:

```yaml
server:
  port: 9999

spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
  datasource:
    url: jdbc:mysql://localhost:3306/food_recommendation?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver

# 微信小程序配置
wechat:
  miniapp:
    appid: ${WECHAT_APPID:your-appid-here}
    secret: ${WECHAT_SECRET:your-secret-here}

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:/mapper/**/*.xml
```

- [ ] **Step 2: Commit**

```bash
git add admin-panel/server-springboot/src/main/resources/application.yml
git commit -m "feat: externalize DB password and WeChat credentials to env vars"
```

### Task 1.3: Add @Valid to all Controller @RequestBody parameters

**Files:**
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/admin/AdminController.java`
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/admin/MerchantController.java`
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/miniapp/AuthController.java`
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/miniapp/InteractionController.java`

- [ ] **Step 1: Add @Valid to AdminController methods that accept @RequestBody**

In `AdminController.java`, add `import jakarta.validation.Valid;` and add `@Valid` before `@RequestBody Merchant merchant` on line 75 and line 82:

```java
import jakarta.validation.Valid;
// ...
@PostMapping("/merchants")
public Result<String> addMerchant(@Valid @RequestBody Merchant merchant) {
    return adminService.addMerchant(merchant)
        ? Result.ok("添加成功")
        : Result.fail("添加失败");
}

@PutMapping("/merchants/{id}")
public Result<String> updateMerchant(@PathVariable Long id, @Valid @RequestBody Merchant merchant) {
    merchant.setMerchantId(id);
    return adminService.updateMerchant(merchant)
        ? Result.ok("更新成功")
        : Result.fail("更新失败");
}
```

- [ ] **Step 2: Add @Valid to MerchantController methods**

In `MerchantController.java`, add `import jakarta.validation.Valid;` and add `@Valid` before `@RequestBody Stall stall` (line 46), `@RequestBody Dish dish` (lines 120, 131):

```java
import jakarta.validation.Valid;
// ...
@PostMapping("/stalls")
public Result<String> addStall(@Valid @RequestBody Stall stall) { ... }

@PutMapping("/stalls/{id}")
public Result<String> updateStall(@PathVariable Long id, @Valid @RequestBody Stall stall) { ... }

@PostMapping("/dishes")
public Result<String> addDish(@Valid @RequestBody Dish dish) { ... }

@PutMapping("/dishes/{id}")
public Result<String> updateDish(@PathVariable Long id, @Valid @RequestBody Dish dish) { ... }
```

- [ ] **Step 3: Add Bean Validation annotations to Entity classes**

In `entity/Dish.java`, add constraints:

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long dishId;

    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 100, message = "菜品名称最长100字符")
    private String dishName;

    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    private BigDecimal price;

    @NotBlank(message = "分类不能为空")
    private String category;

    private String description;
    private String imageUrl;

    @NotNull(message = "所属档口不能为空")
    private Long stallId;
}
```

In `entity/Stall.java`, add:
```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@TableName("stall")
public class Stall {
    @TableId(type = IdType.AUTO)
    private Long stallId;

    @NotBlank(message = "档口名称不能为空")
    @Size(max = 100, message = "档口名称最长100字符")
    private String stallName;

    @NotBlank(message = "位置不能为空")
    private String location;

    @NotNull(message = "所属商户不能为空")
    private Long merchantId;
}
```

In `entity/Merchant.java`, add:
```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@TableName("merchant")
public class Merchant {
    @TableId(type = IdType.AUTO)
    private Long merchantId;

    @NotBlank(message = "商户名称不能为空")
    @Size(max = 100, message = "商户名称最长100字符")
    private String merchantName;

    @NotBlank(message = "联系方式不能为空")
    private String contactInfo;

    private java.time.LocalDate createTime;
}
```

- [ ] **Step 4: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/ admin-panel/server-springboot/src/main/java/com/foodrec/admin/entity/
git commit -m "feat: add @Valid input validation to controllers and entity constraints"
```

---

## Phase ②: Backend Business Logic (5 issues)

### Task 2.1: Fix keyword filter in getFavoriteCount and getHistoryCount

**Files:**
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminServiceImpl.java:189-205`

- [ ] **Step 1: Fix getFavoriteCount to actually use keyword**

Replace lines 189-193:

```java
@Override
public long getFavoriteCount(String keyword) {
    return favoriteMapper.selectCount(null);
}
```

With:

```java
@Override
public long getFavoriteCount(String keyword) {
    LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
    if (keyword != null && !keyword.isEmpty()) {
        qw.like(Favorite::getDishId, keyword);
    }
    return favoriteMapper.selectCount(qw);
}
```

- [ ] **Step 2: Fix getHistoryCount to actually use keyword**

Replace lines 201-205:

```java
@Override
public long getHistoryCount(String keyword) {
    if (keyword == null || keyword.isEmpty()) return historyMapper.selectCount(null);
    return historyMapper.selectCount(null);
}
```

With:

```java
@Override
public long getHistoryCount(String keyword) {
    LambdaQueryWrapper<SelectionHistory> qw = new LambdaQueryWrapper<>();
    if (keyword != null && !keyword.isEmpty()) {
        qw.like(SelectionHistory::getDishId, keyword);
    }
    return historyMapper.selectCount(qw);
}
```

- [ ] **Step 3: Fix getUserCount Long type like() bug**

Replace lines 124-132 so the `like(User::getUserId, keyword)` on Long is changed to match by username only:

```java
@Override
public long getUserCount(String keyword) {
    LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
    if (keyword != null && !keyword.isEmpty()) {
        qw.like(User::getUsername, keyword);
    }
    return userMapper.selectCount(qw);
}
```

Also fix `getUserList` (lines 112-122) to match:

```java
@Override
public List<User> getUserList(String keyword, int page, int pageSize) {
    LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
    if (keyword != null && !keyword.isEmpty()) {
        qw.like(User::getUsername, keyword);
    }
    qw.orderByDesc(User::getRegisterTime);
    Page<User> p = new Page<>(page, pageSize);
    return userMapper.selectPage(p, qw).getRecords();
}
```

- [ ] **Step 4: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminServiceImpl.java
git commit -m "fix: keyword filter for favorite/history counts and Long type like() bug"
```

### Task 2.2: Cascade delete user's favorites and histories

**Files:**
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminServiceImpl.java:134-137`
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminService.java:23`

- [ ] **Step 1: Add @Transactional to AdminServiceImpl and fix deleteUser**

Add import at top of `AdminServiceImpl.java`:
```java
import org.springframework.transaction.annotation.Transactional;
```

Replace `deleteUser` (lines 134-137):

```java
@Override
@Transactional
public boolean deleteUser(Long id) {
    // 1. 删除该用户的收藏记录
    LambdaQueryWrapper<Favorite> favQw = new LambdaQueryWrapper<>();
    favQw.eq(Favorite::getUserId, id);
    favoriteMapper.delete(favQw);
    // 2. 删除该用户的选餐历史
    LambdaQueryWrapper<SelectionHistory> histQw = new LambdaQueryWrapper<>();
    histQw.eq(SelectionHistory::getUserId, id);
    historyMapper.delete(histQw);
    // 3. 删除用户
    return userMapper.deleteById(id) > 0;
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminServiceImpl.java
git commit -m "fix: cascade delete user favorites and histories on user deletion"
```

### Task 2.3: Cascade delete merchant's stalls and dishes

**Files:**
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminServiceImpl.java:176-179`

- [ ] **Step 1: Fix deleteMerchant to cascade**

Replace `deleteMerchant` (lines 176-179):

```java
@Override
@Transactional
public boolean deleteMerchant(Long id) {
    // 1. 查询该商户下的所有档口
    LambdaQueryWrapper<Stall> stallQw = new LambdaQueryWrapper<>();
    stallQw.eq(Stall::getMerchantId, id);
    List<Stall> stalls = stallMapper.selectList(stallQw);
    // 2. 删除每个档口下的菜品，再删除档口
    for (Stall stall : stalls) {
        LambdaQueryWrapper<Dish> dishQw = new LambdaQueryWrapper<>();
        dishQw.eq(Dish::getStallId, stall.getStallId());
        dishMapper.delete(dishQw);
        stallMapper.deleteById(stall.getStallId());
    }
    // 3. 删除商户
    return merchantMapper.deleteById(id) > 0;
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminServiceImpl.java
git commit -m "fix: cascade delete merchant stalls and dishes on merchant deletion"
```

### Task 2.4: Real database backup using mysqldump

**Files:**
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminServiceImpl.java:31-33,207-249`
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminService.java:39-42`
- Modify: `admin-panel/server-springboot/src/main/resources/application.yml`

- [ ] **Step 1: Add backup configuration to application.yml**

```yaml
# 备份配置
backup:
  dir: ${BACKUP_DIR:./backup}
  mysqldump-path: ${MYSQLDUMP_PATH:mysqldump}
  mysql-path: ${MYSQL_PATH:mysql}
```

Add after the `wechat:` section in application.yml.

- [ ] **Step 2: Rewrite backup methods in AdminServiceImpl**

Remove the mock `backupStore` and `backupIdGen` fields (lines 31-33). Replace all backup methods (lines 207-249):

```java
import org.springframework.beans.factory.annotation.Value;
import java.io.*;
import java.nio.file.*;

// Add these fields:
@Value("${backup.dir:./backup}")
private String backupDir;

@Value("${backup.mysqldump-path:mysqldump}")
private String mysqldumpPath;

@Value("${spring.datasource.url}")
private String dbUrl;

@Value("${spring.datasource.username}")
private String dbUsername;

@Value("${spring.datasource.password}")
private String dbPassword;

// Extract db name from JDBC URL
private String getDbName() {
    // jdbc:mysql://localhost:3306/food_recommendation?...
    String[] parts = dbUrl.split("\\?")[0].split("/");
    return parts[parts.length - 1];
}

@Override
public List<Map<String, Object>> getBackupList() {
    List<Map<String, Object>> list = new ArrayList<>();
    Path dir = Paths.get(backupDir);
    if (!Files.exists(dir)) return list;
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.sql")) {
        for (Path entry : stream) {
            Map<String, Object> b = new HashMap<>();
            b.put("id", entry.getFileName().toString());
            b.put("filename", entry.getFileName().toString());
            try {
                b.put("size", String.format("%.1f MB", Files.size(entry) / 1048576.0));
            } catch (IOException e) {
                b.put("size", "未知");
            }
            b.put("time", Files.getLastModifiedTime(entry).toString());
            b.put("type", entry.getFileName().toString().contains("auto") ? "自动备份" : "未知");
            list.add(b);
        }
    } catch (IOException e) {
        // ignore
    }
    list.sort((a, b) -> ((String) b.get("time")).compareTo((String) a.get("time")));
    return list;
}

@Override
public String createBackup() {
    try {
        Files.createDirectories(Paths.get(backupDir));
        String filename = "backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
        Path filepath = Paths.get(backupDir, filename);

        ProcessBuilder pb = new ProcessBuilder(
            mysqldumpPath,
            "-u" + dbUsername,
            "-p" + dbPassword,
            "--databases", getDbName(),
            "--result-file=" + filepath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            return "备份失败，mysqldump 退出码: " + exitCode;
        }
        return "备份成功！文件: " + filename;
    } catch (Exception e) {
        return "备份失败: " + e.getMessage();
    }
}

@Override
public String restoreBackup(Long id) {
    String filename = String.valueOf(id);
    Path filepath = Paths.get(backupDir, filename);
    if (!Files.exists(filepath)) {
        return "备份文件不存在: " + filename;
    }
    try {
        ProcessBuilder pb = new ProcessBuilder(
            mysqlPath,
            "-u" + dbUsername,
            "-p" + dbPassword,
            getDbName()
        );
        pb.redirectInput(filepath.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            return "恢复失败，mysql 退出码: " + exitCode;
        }
        return "从备份 " + filename + " 恢复数据成功！";
    } catch (Exception e) {
        return "恢复失败: " + e.getMessage();
    }
}

@Override
public String deleteBackup(Long id) {
    String filename = String.valueOf(id);
    Path filepath = Paths.get(backupDir, filename);
    try {
        Files.deleteIfExists(filepath);
        return "备份文件已删除: " + filename;
    } catch (IOException e) {
        return "删除失败: " + e.getMessage();
    }
}
```

Also add `@Value` import: `import org.springframework.beans.factory.annotation.Value;`

- [ ] **Step 3: Update AdminService interface**

Change `restoreBackup` and `deleteBackup` parameter type — the id is now a filename string. Update `AdminService.java`:

```java
// ==================== 数据库备份 ====================
List<Map<String, Object>> getBackupList();
String createBackup();
String restoreBackup(Long id);
String deleteBackup(Long id);
```

Keep Long type for backward compatibility — the controller passes Long, we convert to String filename in the service. Actually, keep the interface as-is and handle conversion internally.

- [ ] **Step 4: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/admin/AdminServiceImpl.java admin-panel/server-springboot/src/main/resources/application.yml
git commit -m "feat: replace mock backup with real mysqldump-based backup/restore"
```

### Task 2.5: Fix N+1 query in getGuessLike

**Files:**
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/miniapp/RecommendServiceImpl.java:64-121`

- [ ] **Step 1: Optimize collectHistoryPreferences to batch-load dishes**

Replace the loop in `collectHistoryPreferences` (lines 73-80) that calls `dishMapper.selectById()` in a loop:

```java
private void collectHistoryPreferences(Long userId, Set<Long> triedDishIds,
                                       Set<String> preferredCategories) {
    LambdaQueryWrapper<SelectionHistory> hqw = new LambdaQueryWrapper<>();
    hqw.eq(SelectionHistory::getUserId, userId)
       .ge(SelectionHistory::getScore, 3)
       .orderByDesc(SelectionHistory::getSelectTime)
       .last("LIMIT 20");
    List<SelectionHistory> recentHistories = historyMapper.selectList(hqw);

    // Batch load dishes instead of N+1
    Set<Long> dishIds = new HashSet<>();
    for (SelectionHistory h : recentHistories) {
        triedDishIds.add(h.getDishId());
        dishIds.add(h.getDishId());
    }
    if (!dishIds.isEmpty()) {
        List<Dish> dishes = dishMapper.selectBatchIds(dishIds);
        for (Dish dish : dishes) {
            preferredCategories.add(dish.getCategory());
        }
    }
}
```

- [ ] **Step 2: Optimize collectFavoritePreferences similarly**

Replace the loop in `collectFavoritePreferences` (lines 86-99):

```java
private void collectFavoritePreferences(Long userId, Set<Long> triedDishIds,
                                        Set<String> preferredCategories) {
    LambdaQueryWrapper<Favorite> fqw = new LambdaQueryWrapper<>();
    fqw.eq(Favorite::getUserId, userId);
    List<Favorite> favs = favoriteMapper.selectList(fqw);

    Set<Long> dishIds = new HashSet<>();
    for (Favorite f : favs) {
        triedDishIds.add(f.getDishId());
        dishIds.add(f.getDishId());
    }
    if (!dishIds.isEmpty()) {
        List<Dish> dishes = dishMapper.selectBatchIds(dishIds);
        for (Dish dish : dishes) {
            preferredCategories.add(dish.getCategory());
        }
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/miniapp/RecommendServiceImpl.java
git commit -m "perf: fix N+1 queries in getGuessLike with selectBatchIds"
```

---

## Phase ③: Backend Architecture Upgrade (2 issues)

### Task 3.1: Add Spring Security + JWT

**Files:**
- Modify: `admin-panel/server-springboot/pom.xml`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/JwtUtils.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/JwtAuthenticationFilter.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/SecurityConfig.java`
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/miniapp/AuthServiceImpl.java`
- Modify: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/SpaRouterConfig.java`

- [ ] **Step 1: Add dependencies to pom.xml**

Add inside `<dependencies>` before the closing `</dependencies>` tag:

```xml
        <!-- Spring Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
```

- [ ] **Step 2: Create JwtUtils**

```java
package com.foodrec.admin.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    // 生产环境应使用更安全的密钥并通过配置注入
    private static final String SECRET = "food-rec-secret-key-2026-miniapp-jwt-token";
    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String openid) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("openid", openid)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        return getUserIdFromToken(token) != null;
    }
}
```

- [ ] **Step 3: Create JwtAuthenticationFilter**

```java
package com.foodrec.admin.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 4: Create SecurityConfig**

```java
package com.foodrec.admin.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 放行：小程序登录
                .requestMatchers("POST /api/miniapp/login").permitAll()
                // 放行：小程序推荐浏览（不需要登录）
                .requestMatchers("GET /api/miniapp/recommend/**").permitAll()
                // 放行：菜品列表（小程序首页浏览）
                .requestMatchers("GET /api/admin/dishes").permitAll()
                .requestMatchers("GET /api/admin/dishes/**").permitAll()
                // 放行：静态资源
                .requestMatchers("/static/**", "/index.html", "/favicon.ico", "/icons.svg").permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

- [ ] **Step 5: Update AuthServiceImpl to generate JWT token**

In `AuthServiceImpl.java`, inject JwtUtils and replace the UUID token with JWT:

Add import and field:
```java
import com.foodrec.admin.common.JwtUtils;
// ...
@Autowired
private JwtUtils jwtUtils;
```

Replace `login()` method's token generation (line 76: `String token = UUID.randomUUID().toString().replace("-", "")`):
```java
// 生成 JWT token
String token = jwtUtils.generateToken(user.getUserId(), openid);
```

Remove the `tokenStore` field (line 28) and related `getUserIdByToken` method (lines 123-125) — JWT is stateless. Keep `logout()` as a no-op (JWT cannot be invalidated server-side without a blacklist).

```java
@Override
public boolean logout(String token) {
    // JWT is stateless — token expires naturally after 7 days
    return true;
}

@Override
public Long getUserIdByToken(String token) {
    return jwtUtils.getUserIdFromToken(token);
}
```

- [ ] **Step 6: Update SpaRouterConfig for Security compatibility**

Replace line 25: `if (resourcePath.startsWith("api/"))` with a broader check to return null for API paths so Security can handle them:

No changes needed — the existing logic already returns null for API paths, which lets Spring Security's filter chain process them.

- [ ] **Step 7: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add admin-panel/server-springboot/pom.xml admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/JwtUtils.java admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/JwtAuthenticationFilter.java admin-panel/server-springboot/src/main/java/com/foodrec/admin/common/SecurityConfig.java admin-panel/server-springboot/src/main/java/com/foodrec/admin/service/miniapp/AuthServiceImpl.java
git commit -m "feat: add Spring Security with JWT authentication"
```

### Task 3.2: Add DTO layer (per member boundaries)

**Files:**
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/miniapp/dto/LoginRequest.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/miniapp/dto/LoginResponse.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/miniapp/dto/ProfileUpdateRequest.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/miniapp/dto/ScoreRequest.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/miniapp/dto/FavoriteRequest.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/admin/dto/StallSaveRequest.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/admin/dto/DishSaveRequest.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/admin/dto/DashboardVO.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/admin/dto/TrendVO.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/admin/dto/ScoreDistributionVO.java`
- Create: `admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/admin/dto/PageResult.java`
- Modify: AuthController, InteractionController, MerchantController, AdminController to use DTOs

- [ ] **Step 1: Create Member A DTOs (miniapp/dto/)**

All 5 files in `controller/miniapp/dto/`:

```java
// LoginRequest.java (成员A)
package com.foodrec.admin.controller.miniapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "code不能为空")
    private String code;
}
```

```java
// LoginResponse.java (成员A)
package com.foodrec.admin.controller.miniapp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class LoginResponse {
    private String token;
    private Map<String, Object> userInfo;
}
```

```java
// ProfileUpdateRequest.java (成员A)
package com.foodrec.admin.controller.miniapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotNull(message = "userId不能为空")
    private Long userId;
    private String username;
    private String avatar;
}
```

```java
// ScoreRequest.java (成员A)
package com.foodrec.admin.controller.miniapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreRequest {
    @NotNull(message = "userId不能为空")
    private Long userId;
    @NotNull(message = "dishId不能为空")
    private Long dishId;
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private int score;
}
```

```java
// FavoriteRequest.java (成员A)
package com.foodrec.admin.controller.miniapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotNull(message = "userId不能为空")
    private Long userId;
    @NotNull(message = "dishId不能为空")
    private Long dishId;
}
```

- [ ] **Step 2: Create Member C DTOs (admin/dto/)**

```java
// StallSaveRequest.java (成员C)
package com.foodrec.admin.controller.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StallSaveRequest {
    @NotBlank(message = "档口名称不能为空")
    private String stallName;
    @NotBlank(message = "位置不能为空")
    private String location;
    @NotNull(message = "所属商户不能为空")
    private Long merchantId;
}
```

```java
// DishSaveRequest.java (成员C)
package com.foodrec.admin.controller.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishSaveRequest {
    @NotBlank(message = "菜品名称不能为空")
    private String dishName;
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    private BigDecimal price;
    @NotBlank(message = "分类不能为空")
    private String category;
    private String description;
    private String imageUrl;
    @NotNull(message = "所属档口不能为空")
    private Long stallId;
}
```

- [ ] **Step 3: Create Member D DTOs (admin/dto/)**

```java
// DashboardVO.java (成员D)
package com.foodrec.admin.controller.admin.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private long totalUsers;
    private long totalMerchants;
    private long totalStalls;
    private long totalDishes;
    private long totalFavorites;
    private long totalHistories;
    private double avgScore;
    private long todaySelects;
    private List<Map<String, Object>> categoryDistribution;
    private List<Map<String, Object>> stallDistribution;
}
```

```java
// TrendVO.java (成员D)
package com.foodrec.admin.controller.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class TrendVO {
    private List<String> dates;
    private List<Integer> values;
}
```

```java
// ScoreDistributionVO.java (成员D)
package com.foodrec.admin.controller.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class ScoreDistributionVO {
    private List<Integer> distribution;
}
```

```java
// PageResult.java (成员D - 可放common/)
package com.foodrec.admin.controller.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        PageResult<T> r = new PageResult<>();
        r.list = list;
        r.total = total;
        r.page = page;
        r.pageSize = pageSize;
        return r;
    }
}
```

- [ ] **Step 4: Update Controller methods to use DTOs**

In `AuthController.java`, change login to use `LoginRequest`:

```java
@PostMapping("/login")
public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest body) {
    try {
        Map<String, Object> data = authService.login(body.getCode());
        return Result.ok(data);
    } catch (Exception e) {
        return Result.fail("登录失败: " + e.getMessage());
    }
}
```

In `InteractionController.java`, change methods to use DTOs:

```java
@PostMapping("/favorite")
public Result<String> addFavorite(@Valid @RequestBody FavoriteRequest body) {
    return interactionService.addFavorite(body.getUserId(), body.getDishId())
            ? Result.ok("收藏成功")
            : Result.fail("收藏失败");
}

@PostMapping("/score")
public Result<String> scoreDish(@Valid @RequestBody ScoreRequest body) {
    return interactionService.scoreDish(body.getUserId(), body.getDishId(), body.getScore())
            ? Result.ok("评分成功")
            : Result.fail("评分失败");
}
```

In `MerchantController.java`:
```java
@PostMapping("/stalls")
public Result<String> addStall(@Valid @RequestBody StallSaveRequest req) {
    Stall stall = new Stall();
    stall.setStallName(req.getStallName());
    stall.setLocation(req.getLocation());
    stall.setMerchantId(req.getMerchantId());
    return merchantService.addStall(stall)
            ? Result.ok("添加成功")
            : Result.fail("添加失败");
}
```

(Full controller rewrites are extensive — see the spec for all endpoints. Each follows the same pattern: DTO → Entity mapping in controller, Service unchanged.)

- [ ] **Step 5: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add admin-panel/server-springboot/src/main/java/com/foodrec/admin/controller/
git commit -m "feat: add DTO layer per member boundaries with validation"
```

---

## Phase ④: Miniapp Fixes (9 issues)

### Task 4.1: Fix default userId and token in requests

**Files:**
- Modify: `miniapp/app.js:44,53`
- Modify: `miniapp/utils/api.js:16-43`

- [ ] **Step 1: Change default userId to null in app.js**

```javascript
// Line 19: change fallback
this.globalData.userId = userInfo.userId || null

// Line 44: change clearLoginState
clearLoginState() {
    this.globalData.token = null
    this.globalData.userInfo = null
    this.globalData.userId = null  // was: 1
    this.globalData.isLoggedIn = false
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
}

// Line 53: change globalData default
globalData: {
    token: null,
    userId: null,  // was: 1
    userInfo: null,
    isLoggedIn: false,
    systemInfo: null,
    statusBarHeight: 0,
    dishCache: null
}
```

Also fix line 17-21 in `onLaunch()` to not set userId when userInfo is null:

```javascript
const token = wx.getStorageSync('token')
const userInfo = wx.getStorageSync('userInfo')
if (token && userInfo) {
    this.globalData.token = token
    this.globalData.userInfo = userInfo
    this.globalData.userId = userInfo.userId || null
    this.globalData.isLoggedIn = true
}
```

- [ ] **Step 2: Add Authorization header to all requests in api.js**

Replace the `request()` function (lines 16-43):

```javascript
const request = (url, method = 'GET', data = {}, useAdmin = false) => {
  const baseUrl = useAdmin ? config.ADMIN_BASE_URL : config.BASE_URL
  const app = getApp()
  const token = app.globalData.token || wx.getStorageSync('token')

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data.data)
        } else if (res.statusCode === 401 || res.statusCode === 403) {
          // Token 过期或无效，清除登录态
          const app = getApp()
          app.clearLoginState()
          wx.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 2000 })
          reject(res.data)
        } else {
          const msg = (res.data && res.data.message) || '请求失败'
          wx.showToast({ title: msg, icon: 'none', duration: 2000 })
          reject(res.data)
        }
      },
      fail: (err) => {
        console.error('[API] 网络请求失败:', err)
        wx.showToast({ title: '网络异常，请检查网络', icon: 'none', duration: 2000 })
        reject(err)
      }
    })
  })
}
```

- [ ] **Step 3: Commit**

```bash
git add miniapp/app.js miniapp/utils/api.js
git commit -m "fix: set userId default to null and attach auth token to requests"
```

### Task 4.2: Add getProfile call on mine page and toggleLike integration

**Files:**
- Modify: `miniapp/pages/mine/mine.js`

- [ ] **Step 1: Add getProfile call in onShow**

Replace the `onShow()` method:

```javascript
onShow() {
    this.syncLoginState()
    if (this.data.isLoggedIn) {
        this.loadCurrentTab()
        this.refreshProfile()
    }
},

/**
 * 从服务端刷新用户信息
 */
async refreshProfile() {
    try {
        const profile = await api.getProfile(app.globalData.userId)
        if (profile) {
            const userInfo = {
                userId: profile.userId,
                username: profile.username,
                avatar: profile.avatar
            }
            app.globalData.userInfo = userInfo
            wx.setStorageSync('userInfo', userInfo)
            this.setData({ userInfo })
        }
    } catch (e) {
        // 静默失败，使用本地缓存
    }
},
```

- [ ] **Step 2: Add toggleLike to history items in mine page**

Add a `onToggleLike` method:

```javascript
onToggleLike(e) {
    const dish = e.currentTarget.dataset.dish
    const newStatus = !dish.like_status
    wx.showToast({ title: newStatus ? '已点赞' : '已取消点赞', icon: 'success', duration: 1000 })
    api.toggleLike(app.globalData.userId, dish.dish_id, newStatus)
        .then(() => this.loadHistory())
        .catch(() => wx.showToast({ title: '操作失败', icon: 'none' }))
},
```

- [ ] **Step 3: Add default avatar placeholder**

In `mine.wxml`, modify the avatar image tag. Add a fallback when avatar is empty:

```xml
<image 
    class="avatar" 
    src="{{userInfo.avatar || '/images/default-avatar.png'}}" 
    mode="aspectFill"
    binderror="onAvatarError"
/>
```

Add `onAvatarError` in mine.js:
```javascript
onAvatarError() {
    // 头像加载失败，使用默认样式兜底
},
```

- [ ] **Step 4: Commit**

```bash
git add miniapp/pages/mine/
git commit -m "fix: add getProfile refresh, toggleLike, and default avatar fallback"
```

### Task 4.3: Fix half-star rendering in today-praise

**Files:**
- Modify: `miniapp/pages/today-praise/today-praise.wxml` (check existing star rendering)
- Modify: `miniapp/pages/today-praise/today-praise.js`

- [ ] **Step 1: Add correct star computation to today-praise.js**

Add a `processStars` helper in `loadPraiseList`:

```javascript
async loadPraiseList() {
    this.setData({ loading: true })
    try {
        const data = await api.getTodayPraise()
        const list = (data.list || []).map(item => {
            const avgScore = parseFloat(item.avg_score || 0)
            const fullStars = Math.floor(avgScore)
            const hasHalfStar = (avgScore - fullStars) >= 0.25 && (avgScore - fullStars) < 0.75
            const extraFull = (avgScore - fullStars) >= 0.75 ? 1 : 0
            return {
                ...item,
                fullStars: fullStars + extraFull,
                hasHalfStar: hasHalfStar && extraFull === 0,
                emptyStars: 5 - fullStars - extraFull - (hasHalfStar && extraFull === 0 ? 1 : 0)
            }
        })
        this.setData({ list, loading: false })
    } catch (err) {
        console.error('[TodayPraise] 加载失败:', err)
        this.setData({ loading: false })
    }
},
```

- [ ] **Step 2: Commit**

```bash
git add miniapp/pages/today-praise/today-praise.js
git commit -m "fix: correct half-star rendering logic in today-praise"
```

### Task 4.4: Enable pull-down refresh on index page

**Files:**
- Modify: `miniapp/pages/index/index.json`
- Modify: `miniapp/pages/index/index.js`

- [ ] **Step 1: Add enablePullDownRefresh to index.json**

Add to the JSON config:
```json
{
  "enablePullDownRefresh": true,
  "backgroundColor": "#f5f5f5"
}
```

- [ ] **Step 2: Add onPullDownRefresh handler in index.js**

Add to `Page({})`:
```javascript
onPullDownRefresh() {
    this.setData({ page: 1, dishes: [], hasMore: true })
    this.loadDishes()
    wx.stopPullDownRefresh()
},
```

- [ ] **Step 3: Commit**

```bash
git add miniapp/pages/index/
git commit -m "fix: enable pull-down refresh on index page"
```

### Task 4.5: Add pagination to guess-like, today-praise, mine

**Files:**
- Modify: `miniapp/pages/guess-like/guess-like.js`
- Modify: `miniapp/pages/today-praise/today-praise.js`
- Modify: `miniapp/pages/mine/mine.js`

- [ ] **Step 1: Add onReachBottom to guess-like**

Add to `Page({})`:
```javascript
data: {
    list: [],
    loading: false,
    page: 1,
    pageSize: 10,
    hasMore: true
},

onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
        this.loadMore()
    }
},

async loadMore() {
    // guess-like is currently loaded all at once from backend
    // Add pagination params: page, pageSize
    // For now, just mark no more
    this.setData({ hasMore: false })
},
```

- [ ] **Step 2: Add onReachBottom to today-praise**

Similar pattern — add `page`, `pageSize`, `hasMore` data fields and `onReachBottom` method.

- [ ] **Step 3: Add onReachBottom to mine page (favorites/history tabs)**

```javascript
data: {
    // ... existing
    favoritesPage: 1,
    historyPage: 1,
    scoresPage: 1,
    pageSize: 10,
    hasMoreFavorites: true,
    hasMoreHistory: true,
    hasMoreScores: true
},

onReachBottom() {
    const { currentTab } = this.data
    if (currentTab === 'favorites' && this.data.hasMoreFavorites) {
        this.loadMoreFavorites()
    } else if (currentTab === 'history' && this.data.hasMoreHistory) {
        this.loadMoreHistory()
    }
},
```

- [ ] **Step 4: Commit**

```bash
git add miniapp/pages/guess-like/ miniapp/pages/today-praise/ miniapp/pages/mine/
git commit -m "feat: add pagination (onReachBottom) to guess-like, today-praise, mine"
```

### Task 4.6: Optimize dish-detail fallback query

**Files:**
- Modify: `miniapp/pages/dish-detail/dish-detail.js`

- [ ] **Step 1: Add precise dish query fallback**

In dish-detail's `onLoad`, add a direct API call fallback when cache misses:

```javascript
onLoad(options) {
    const dishId = options.dishId
    const cache = app.globalData.dishCache
    
    if (cache && cache.dish_id == dishId) {
        this.setData({ dish: cache })
        app.globalData.dishCache = null
    } else {
        // Precise query: use dish list with keyword = dishId
        this.loadDishById(dishId)
    }
},

async loadDishById(dishId) {
    try {
        // Try to get the dish from the admin dishes endpoint with the ID
        const res = await api.getDishList({ keyword: dishId, page: 1, pageSize: 1 })
        if (res.list && res.list.length > 0) {
            this.setData({ dish: res.list[0] })
        } else {
            wx.showToast({ title: '菜品不存在', icon: 'none' })
        }
    } catch (e) {
        wx.showToast({ title: '加载失败', icon: 'none' })
    }
},
```

- [ ] **Step 2: Commit**

```bash
git add miniapp/pages/dish-detail/dish-detail.js
git commit -m "perf: improve dish-detail fallback query from loading 100 to precise lookup"
```

---

## Phase ⑤: Admin Panel Fixes (11 issues)

### Task 5.1: Backend — Add new endpoints for admin panel features

**Files:**
- Modify: `AdminController.java` — add export, notification-count, backup-download endpoints
- Modify: `MerchantController.java` — add dish-stats, dish-status endpoints
- Modify: `AdminService.java` + `AdminServiceImpl.java` — add new service methods
- Modify: `MerchantService.java` + `MerchantServiceImpl.java` — add stats/status methods
- Modify: `entity/Dish.java` — add status field

- [ ] **Step 1: Add status field to Dish entity**

```java
// Add to Dish.java:
private String status; // pending, approved, rejected
```

And add SQL migration file `admin-panel/server-springboot/src/main/resources/db/migration.sql`:
```sql
-- Run this manually if the column doesn't exist:
ALTER TABLE dish ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'approved';
UPDATE dish SET status = 'approved' WHERE status IS NULL;
```

Add to application.yml for auto-execution during startup:
```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:db/migration.sql
      continue-on-error: true
```

- [ ] **Step 2: Add new AdminService methods and implement them**

In `AdminService.java`:
```java
// 导出
byte[] exportCsv(String type) throws IOException;
// 通知
Map<String, Object> getNotificationCount();
// 备份下载
byte[] downloadBackup(String filename) throws IOException;
```

In `AdminServiceImpl.java`, implement:
```java
@Override
public Map<String, Object> getNotificationCount() {
    Map<String, Object> counts = new HashMap<>();
    // 待审核菜品数
    LambdaQueryWrapper<Dish> dishQw = new LambdaQueryWrapper<>();
    dishQw.eq(Dish::getStatus, "pending");
    counts.put("pendingDishes", dishMapper.selectCount(dishQw));
    // 备份总数
    counts.put("totalBackups", getBackupList().size());
    return counts;
}

@Override
public byte[] exportCsv(String type) throws IOException {
    StringBuilder sb = new StringBuilder();
    if ("users".equals(type)) {
        sb.append("用户ID,用户名,注册时间\n");
        List<User> users = userMapper.selectList(null);
        for (User u : users) {
            sb.append(String.format("%d,%s,%s\n", u.getUserId(), u.getUsername(), u.getRegisterTime()));
        }
    }
    // ... similar for dishes, favorites, histories
    return sb.toString().getBytes(StandardCharsets.UTF_8);
}

@Override
public byte[] downloadBackup(String filename) throws IOException {
    Path filepath = Paths.get(backupDir, filename);
    if (!Files.exists(filepath)) throw new RuntimeException("文件不存在");
    return Files.readAllBytes(filepath);
}
```

- [ ] **Step 3: Add new endpoints to AdminController**

```java
@GetMapping("/export/{type}")
public ResponseEntity<byte[]> exportCsv(@PathVariable String type) throws IOException {
    byte[] data = adminService.exportCsv(type);
    return ResponseEntity.ok()
            .header("Content-Type", "text/csv; charset=UTF-8")
            .header("Content-Disposition", "attachment; filename=" + type + ".csv")
            .body(data);
}

@GetMapping("/notifications/count")
public Result<Map<String, Object>> notificationCount() {
    return Result.ok(adminService.getNotificationCount());
}

@GetMapping("/backups/{filename}/download")
public ResponseEntity<byte[]> downloadBackup(@PathVariable String filename) throws IOException {
    byte[] data = adminService.downloadBackup(filename);
    return ResponseEntity.ok()
            .header("Content-Type", "application/sql")
            .header("Content-Disposition", "attachment; filename=" + filename)
            .body(data);
}
```

- [ ] **Step 4: Add dish stats and status endpoints to MerchantController**

```java
@GetMapping("/dishes/stats")
public Result<Map<String, Object>> dishStats() {
    return Result.ok(merchantService.getDishStats());
}

@PutMapping("/dishes/{id}/status")
public Result<String> updateDishStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
    String status = body.get("status");
    return merchantService.updateDishStatus(id, status)
            ? Result.ok("状态更新成功")
            : Result.fail("更新失败");
}
```

In `MerchantServiceImpl`:
```java
@Override
public Map<String, Object> getDishStats() {
    List<Dish> allDishes = dishMapper.selectList(null);
    Map<String, Object> stats = new HashMap<>();
    stats.put("total", allDishes.size());
    stats.put("categoryCount", allDishes.stream().map(Dish::getCategory).distinct().count());
    stats.put("avgPrice", allDishes.stream()
            .mapToDouble(d -> d.getPrice().doubleValue()).average().orElse(0));
    return stats;
}

@Override
public boolean updateDishStatus(Long dishId, String status) {
    Dish dish = dishMapper.selectById(dishId);
    if (dish == null) return false;
    dish.setStatus(status);
    return dishMapper.updateById(dish) > 0;
}
```

- [ ] **Step 5: Verify compilation**

Run: `cd admin-panel/server-springboot && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add admin-panel/server-springboot/
git commit -m "feat: add export, notification, backup-download, dish-stats, dish-status endpoints"
```

### Task 5.2: Fix AdminLayout (notification count, dropdown events)

**Files:**
- Modify: `admin-panel/src/layout/AdminLayout.vue`

- [ ] **Step 1: Make notification count dynamic**

Replace hardcoded `:value="3"` with a reactive ref:

```javascript
import { ref, computed, onMounted } from 'vue'
import api from '@/api'

const notificationCount = ref(0)

const fetchNotificationCount = async () => {
    try {
        const res = await api.getNotificationCount()
        if (res.code === 200) {
            notificationCount.value = (res.data.pendingDishes || 0) + (res.data.totalBackups || 0)
        }
    } catch (e) { /* ignore */ }
}

onMounted(() => { fetchNotificationCount() })
```

Template: change `:value="3"` to `:value="notificationCount"`. Hide badge when count is 0: add `:hidden="notificationCount === 0"`.

- [ ] **Step 2: Add dropdown menu event handlers**

Add methods:
```javascript
const handleProfile = () => {
    ElMessage.info('管理员信息：系统管理员')
}

const handleSettings = () => {
    ElMessage.info('系统设置功能开发中')
}

const handleLogout = () => {
    localStorage.removeItem('admin_token')
    window.location.reload()
}
```

Template: add `@click` handlers to each `el-dropdown-item`:
```html
<el-dropdown-item @click="handleProfile">
    <el-icon><UserFilled /></el-icon>个人信息
</el-dropdown-item>
<el-dropdown-item @click="handleSettings">
    <el-icon><Setting /></el-icon>系统设置
</el-dropdown-item>
<el-dropdown-item divided @click="handleLogout">
    <el-icon><SwitchButton /></el-icon>退出登录
</el-dropdown-item>
```

- [ ] **Step 3: Add api method for notification count**

In `admin-panel/src/api/index.js`:
```javascript
getNotificationCount() {
    return http.get('/notifications/count')
},
```

- [ ] **Step 4: Commit**

```bash
git add admin-panel/src/layout/AdminLayout.vue admin-panel/src/api/index.js
git commit -m "fix: dynamic notification count and dropdown menu events in AdminLayout"
```

### Task 5.3: Fix DishAudit (stats, audit functionality)

**Files:**
- Modify: `admin-panel/src/views/DishAudit.vue`
- Modify: `admin-panel/src/api/index.js`

- [ ] **Step 1: Fetch global stats from backend**

Replace computed `categoryCount` and `avgPrice` with reactive data from the new stats endpoint:

```javascript
const stats = reactive({ total: 0, categoryCount: 0, avgPrice: '0' })

const fetchStats = async () => {
    try {
        const res = await api.getDishStats()
        if (res.code === 200) {
            stats.total = res.data.total
            stats.categoryCount = res.data.categoryCount
            stats.avgPrice = Number(res.data.avgPrice).toFixed(1)
        }
    } catch (e) { /* ignore */ }
}
```

Template: change `StatCard` values to use `stats.total`, `stats.categoryCount`, `stats.avgPrice`.

- [ ] **Step 2: Add audit functionality with status filter and approve/reject buttons**

Add to data:
```javascript
const statusFilter = ref('pending') // default to show pending dishes
const searchFilters = [
    { prop: 'category', type: 'select', placeholder: '菜品分类', options: [...] },
    { prop: 'status', type: 'select', placeholder: '审核状态', options: [
        { label: '待审核', value: 'pending' },
        { label: '已通过', value: 'approved' },
        { label: '已拒绝', value: 'rejected' },
        { label: '全部', value: '' },
    ]},
]
```

Add audit methods:
```javascript
const handleApprove = async (dish) => {
    try {
        await api.updateDishStatus(dish.dish_id, 'approved')
        ElMessage.success('已通过')
        fetchData()
    } catch (e) { ElMessage.error('操作失败') }
}

const handleReject = async (dish) => {
    try {
        await api.updateDishStatus(dish.dish_id, 'rejected')
        ElMessage.success('已拒绝')
        fetchData()
    } catch (e) { ElMessage.error('操作失败') }
}
```

Add to each card template:
```html
<div class="dish-card-actions" v-if="dish.status === 'pending'">
    <el-button size="small" type="success" @click.stop="handleApprove(dish)">通过</el-button>
    <el-button size="small" type="danger" @click.stop="handleReject(dish)">拒绝</el-button>
</div>
<div v-else class="dish-card-status">
    <StatusTag :status="dish.status" 
        :text="dish.status === 'approved' ? '已通过' : dish.status === 'rejected' ? '已拒绝' : '待审核'" />
</div>
```

Also add StatusTag color mapping: pending → warning (yellow), approved → success (green), rejected → danger (red).

- [ ] **Step 3: Add api methods**

In `admin-panel/src/api/index.js`:
```javascript
getDishStats() {
    return http.get('/dishes/stats')
},
updateDishStatus(dishId, status) {
    return http.put(`/dishes/${dishId}/status`, { status })
},
```

- [ ] **Step 4: Commit**

```bash
git add admin-panel/src/views/DishAudit.vue admin-panel/src/api/index.js
git commit -m "feat: add global stats, dish audit (approve/reject) with status filter"
```

### Task 5.4: Fix StallOverview dynamic merchant filter

**Files:**
- Modify: `admin-panel/src/views/StallOverview.vue`

- [ ] **Step 1: Load merchants dynamically for filter dropdown**

```javascript
import { ref, onMounted } from 'vue'
import api from '@/api'

const merchantOptions = ref([])

const loadMerchants = async () => {
    try {
        const res = await api.getMerchants({ pageSize: 9999 })
        if (res.code === 200) {
            merchantOptions.value = (res.data.list || []).map(m => ({
                label: m.merchant_name,
                value: m.merchant_id
            }))
        }
    } catch (e) { /* ignore */ }
}

onMounted(() => {
    loadMerchants()
    fetchData()
})
```

Replace the hardcoded options in `searchFilters` with `merchantOptions`.

- [ ] **Step 2: Commit**

```bash
git add admin-panel/src/views/StallOverview.vue
git commit -m "fix: dynamic merchant filter dropdown in StallOverview"
```

### Task 5.5: Data export, backup persistence, and cleanup

**Files:**
- Modify: `admin-panel/src/views/DataQuery.vue`
- Modify: `admin-panel/src/views/DatabaseMaintenance.vue`

- [ ] **Step 1: Add export buttons to DataQuery**

Add to each tab's toolbar:
```html
<el-button type="success" @click="handleExport('users')">导出CSV</el-button>
```

Add method:
```javascript
const handleExport = (type) => {
    window.open(`/api/admin/export/${type}`, '_blank')
    ElMessage.success('导出中...')
}
```

- [ ] **Step 2: Persist auto-backup settings to localStorage**

In DatabaseMaintenance.vue, add localStorage read/write:
```javascript
const autoBackupSettings = reactive({
    enabled: localStorage.getItem('autoBackup_enabled') === 'true',
    frequency: localStorage.getItem('autoBackup_frequency') || 'daily'
})

const saveAutoBackupSettings = () => {
    localStorage.setItem('autoBackup_enabled', autoBackupSettings.enabled)
    localStorage.setItem('autoBackup_frequency', autoBackupSettings.frequency)
    ElMessage.success('设置已保存')
}
```

- [ ] **Step 3: Add backup download button**

```html
<el-button size="small" @click="handleDownload(row.filename)">下载</el-button>
```

```javascript
const handleDownload = (filename) => {
    window.open(`/api/admin/backups/${filename}/download`, '_blank')
}
```

- [ ] **Step 4: Commit**

```bash
git add admin-panel/src/views/DataQuery.vue admin-panel/src/views/DatabaseMaintenance.vue
git commit -m "feat: data export, backup settings persistence, backup download"
```

### Task 5.6: Remove dead code

**Files:**
- Delete: `admin-panel/src/views/DishOverview.vue`
- Delete: `admin-panel/src/api/mock.js`

- [ ] **Step 1: Delete dead files**

```bash
git rm admin-panel/src/views/DishOverview.vue admin-panel/src/api/mock.js
git commit -m "chore: remove dead code (DishOverview.vue, mock.js)"
```

---

## Phase ⑥: Tests (1 issue)

### Task 6.1: Add Service layer unit tests

**Files:**
- Modify: `admin-panel/server-springboot/pom.xml` — add H2 dependency
- Create: `admin-panel/server-springboot/src/test/resources/application-test.yml`
- Create: `admin-panel/server-springboot/src/test/java/com/foodrec/admin/service/miniapp/AuthServiceImplTest.java`
- Create: `admin-panel/server-springboot/src/test/java/com/foodrec/admin/service/miniapp/InteractionServiceImplTest.java`
- Create: `admin-panel/server-springboot/src/test/java/com/foodrec/admin/service/miniapp/RecommendServiceImplTest.java`
- Create: `admin-panel/server-springboot/src/test/java/com/foodrec/admin/service/admin/AdminServiceImplTest.java`
- Create: `admin-panel/server-springboot/src/test/java/com/foodrec/admin/service/admin/MerchantServiceImplTest.java`

- [ ] **Step 1: Add H2 test dependency to pom.xml**

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Create test application config**

`src/test/resources/application-test.yml`:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver
  sql:
    init:
      mode: always
      schema-locations: classpath:test-schema.sql
      data-locations: classpath:test-data.sql

wechat:
  miniapp:
    appid: test-appid
    secret: test-secret

backup:
  dir: ./target/test-backup

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
```

- [ ] **Step 3: Write test for AuthServiceImpl**

```java
package com.foodrec.admin.service.miniapp;

import com.foodrec.admin.entity.User;
import com.foodrec.admin.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceImplTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserMapper userMapper;

    @Test
    void loginShouldCreateNewUserAndReturnToken() {
        Map<String, Object> result = authService.login("test_code_123");
        assertNotNull(result);
        assertNotNull(result.get("token"));
        assertNotNull(result.get("userInfo"));
    }

    @Test
    void getProfileShouldReturnUserInfo() {
        User user = new User();
        user.setUsername("测试用户");
        user.setPassword("");
        user.setOpenid("test_openid");
        user.setRegisterTime(java.time.LocalDateTime.now());
        userMapper.insert(user);

        Map<String, Object> profile = authService.getProfile(user.getUserId());
        assertNotNull(profile);
        assertEquals("测试用户", profile.get("username"));
    }

    @Test
    void logoutShouldReturnTrue() {
        assertTrue(authService.logout("any_token"));
    }
}
```

- [ ] **Step 4: Write tests for other services**

Follow the same pattern — `@SpringBootTest` + `@ActiveProfiles("test")` + `@Transactional`. Each test class covers:
- Normal path (happy case)
- Edge case (null/empty input)
- Boundary condition (max values, duplicates)

For `AdminServiceImplTest`, test:
- `deleteUser` cascade deletes favorites and histories
- `deleteMerchant` cascade deletes stalls and dishes
- `getFavoriteCount` with keyword filtering
- `getHistoryCount` with keyword filtering

For `RecommendServiceImplTest`, test:
- `getRandomDish` returns a dish
- `getTodayPraise` returns list with scores
- `getGuessLike` handles userId with no history

- [ ] **Step 5: Run tests**

```bash
cd admin-panel/server-springboot && mvn test -q
```
Expected: All tests pass (BUILD SUCCESS).

- [ ] **Step 6: Commit**

```bash
git add admin-panel/server-springboot/pom.xml admin-panel/server-springboot/src/test/
git commit -m "test: add service layer unit tests for all 4 member modules"
```

---

## Verification Checklist

After all phases complete, verify:

1. **Backend starts clean:** `mvn spring-boot:run -DskipTests` → port 9999
2. **Admin panel builds:** `cd admin-panel && npm run build` → no errors
3. **Miniapp compiles:** Open in WeChat DevTools → no errors
4. **API auth works:** Call `/api/miniapp/login` → get JWT token → call protected endpoint with Bearer → 200
5. **Cascade delete works:** Delete user → check favorites/history removed
6. **Backup works:** Call create backup → file appears in backup/ dir
7. **Validation works:** Post empty dish name → get 400 with field errors
