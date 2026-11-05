; ---------------------------------------------------------------------------
; Nimay OMP Bridge - FRESH installer (from scratch)
; No voice, no stemmemodeller, no admin, no SYSTEM, no Program Files.
; ---------------------------------------------------------------------------

#define MyAppName "Nimay OMP Bridge"
#define MyAppVersion "3.3.1"
#define MyAppPublisher "Nimay"

[Setup]
AppId={{B1C2D3E4-F5A6-47B8-9C0D-1E2F3A4B5C6D}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={localappdata}\Nimay\OMPBridge
DisableProgramGroupPage=yes
OutputDir=dist
OutputBaseFilename=Nimay-OMP-Bridge-Setup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=lowest
DisableWelcomePage=no
CloseApplications=no
MinVersion=10.0

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "norwegian"; MessagesFile: "compiler:Languages\Norwegian.isl"

[Files]
Source: "build-new\payload\bridge\*"; DestDir: "{app}\bridge"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "build-new\payload\tools\start-bridge-hidden.ps1"; DestDir: "{app}\tools"; Flags: ignoreversion
Source: "build-new\payload\commands\*"; DestDir: "{%USERPROFILE}\.omp\agent\commands"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{userstartup}\Nimay OMP Bridge"; Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File ""{app}\tools\start-bridge-hidden.ps1"""; WorkingDir: "{app}"; Comment: "Starter Nimay OMP Bridge"

[Run]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File ""{app}\tools\start-bridge-hidden.ps1"""; WorkingDir: "{app}"; Flags: nowait postinstall skipifsilent; Description: "Start Bridge now"

[UninstallRun]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File ""{app}\tools\stop-bridge.ps1"" -InstallDir ""{app}"""; Flags: runhidden; RunOnceId: "StopBridge"

[Code]
var
  MobilePrepared: Boolean;

function FindOmpBinary: String;
var
  bunExe: String;
  cliJs: String;
  bunExe2: String;
  ompCmd: String;
  ompExe: String;
  ResultCode: Integer;
  TempFile: String;
  Lines: TArrayOfString;
  I: Integer;
begin
  Result := '';
  // 1. Bun-based OMP (preferred) - try multiple bun locations
  cliJs := GetEnv('USERPROFILE') + '\.bun\install\global\node_modules\@oh-my-pi\pi-coding-agent\dist\cli.js';
  // Diagnostic: write paths to a file
  if FileExists(cliJs) then SaveStringToFile(ExpandConstant('{tmp}\ompdiag1.txt'), 'cliJs YES', False) else SaveStringToFile(ExpandConstant('{tmp}\ompdiag1.txt'), 'cliJs NO', False);
  if FileExists(GetEnv('LOCALAPPDATA') + '\Microsoft\WinGet\Packages\Oven-sh.Bun_Microsoft.Winget.Source_8wekyb3d8bbwe\bun-windows-x64\bun.exe') then SaveStringToFile(ExpandConstant('{tmp}\ompdiag2.txt'), 'bunExe YES', False) else SaveStringToFile(ExpandConstant('{tmp}\ompdiag2.txt'), 'bunExe NO', False);
  // Diagnostic: write paths to bridge dir
  SaveStringToFile(ExpandConstant('{app}\bridge\omp-resolve.log'), 'USERPROFILE=' + GetEnv('USERPROFILE') + #13#10 + 'cliJsPath=' + cliJs + #13#10, False);

  if FileExists(cliJs) then
  begin
    bunExe := GetEnv('LOCALAPPDATA') + '\Microsoft\WinGet\Packages\Oven-sh.Bun_Microsoft.Winget.Source_8wekyb3d8bbwe\bun-windows-x64\bun.exe';
    if FileExists(bunExe) then
    begin
      SaveStringToFile(ExpandConstant('{app}\bridge\omp-resolve.log'), 'bunExe=YES' + #13#10, True);
      Result := bunExe + '|' + cliJs;
      Exit;
    end
    else
    begin
      SaveStringToFile(ExpandConstant('{app}\bridge\omp-resolve.log'), 'bunExe=NO' + #13#10, True);
    end;
    bunExe := GetEnv('USERPROFILE') + '\.bun\bin\bun.exe';
    if FileExists(bunExe) then begin Result := bunExe + '|' + cliJs; Exit; end;
  end;
  // 2. Search PATH for omp.cmd / omp.exe
  TempFile := ExpandConstant('{tmp}\where-omp.txt');
  Exec(ExpandConstant('{sys}\cmd.exe'), '/c where omp.cmd > "' + TempFile + '" 2>nul', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  if LoadStringsFromFile(TempFile, Lines) then
    for I := 0 to GetArrayLength(Lines) - 1 do
      if FileExists(Trim(Lines[I])) then
      begin
        Result := Trim(Lines[I]);
        Exit;
      end;
  TempFile := ExpandConstant('{tmp}\where-omp2.txt');
  Exec(ExpandConstant('{sys}\cmd.exe'), '/c where omp.exe > "' + TempFile + '" 2>nul', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  if LoadStringsFromFile(TempFile, Lines) then
    for I := 0 to GetArrayLength(Lines) - 1 do
      if FileExists(Trim(Lines[I])) then
      begin
        Result := Trim(Lines[I]);
        Exit;
      end;
  // 3. Common locations
  ompCmd := GetEnv('APPDATA') + '\npm\omp.cmd';
  if FileExists(ompCmd) then begin Result := ompCmd; Exit; end;
  ompExe := GetEnv('USERPROFILE') + '\.bun\bin\omp.exe';
  if FileExists(ompExe) then begin Result := ompExe; Exit; end;
  ompCmd := GetEnv('USERPROFILE') + '\.bun\bin\omp.cmd';
  if FileExists(ompCmd) then begin Result := ompCmd; Exit; end;
  // 4. Bun shim as last resort
  Result := GetEnv('USERPROFILE') + '\.bun\bin\omp.exe';
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  BridgePath: String;
  ConfigPath: String;
  Token: String;
  OmpBin: String;
  ResultCode: Integer;
begin
  if CurStep = ssPostInstall then
  begin
    BridgePath := ExpandConstant('{app}\bridge');
    ConfigPath := BridgePath + '\config.json';

    // Create config.json if it doesn't exist (preserve existing token on upgrade)
    if not FileExists(ConfigPath) then
    begin
      Token := 'nimay-' + IntToStr(Random(999999)) + '-' + IntToStr(Random(99999));
      SaveStringToFile(ConfigPath,
        '{' + #13#10 +
        '  "port": 7777,' + #13#10 +
        '  "host": "0.0.0.0",' + #13#10 +
        '  "token": "' + Token + '",' + #13#10 +
        '  "approvalMode": "yolo",' + #13#10 +
        '  "allowBash": true,' + #13#10 +
        '  "projectRoots": [],' + #13#10 +
        '  "maxChildren": 4,' + #13#10 +
        '  "idleMinutes": 90' + #13#10 +
        '}', False);
    end;

    // Find OMP binary robustly
    OmpBin := FindOmpBinary;
    if OmpBin <> '' then
    begin
      RegWriteStringValue(HKCU, 'Environment', 'OMP_BIN', OmpBin);
      // Also write to a file the bridge/ompchat can read without env refresh
      SaveStringToFile(BridgePath + '\omp-bin.txt', OmpBin, False);
    end;

    // Register autostart via registry Run key (user context, no admin)
    RegWriteStringValue(HKCU, 'Software\Microsoft\Windows\CurrentVersion\Run', 'Nimay OMP Bridge', 'wscript.exe "' + ExpandConstant('{app}\tools\start-bridge-hidden.vbs') + '"');

    // Copy OMP commands
    Exec('cmd.exe', '/c xcopy /E /I /Y "' + ExpandConstant('{app}\commands') + '" "' + GetEnv('USERPROFILE') + '\.omp\agent\commands"', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);

    // Start bridge in background (user context, no console)
    Exec('wscript.exe', '"' + ExpandConstant('{app}\tools\start-bridge-hidden.vbs') + '"', ExpandConstant('{app}\tools'), SW_HIDE, ewNoWait, ResultCode);
  end;
end;
