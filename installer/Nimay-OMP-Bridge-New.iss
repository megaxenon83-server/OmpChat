#define Payload "build-new\payload"
; Clean, standalone installer from scratch

#define MyAppName "Nimay OMP Bridge"
#define MyAppVersion "3.3.0"
#define MyAppPublisher "Nimay"

[Setup]
AppId={{E5F6A7B8-C9D0-4E1F-2A3B-4C5D6E7F8A9B}}
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

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "norwegian"; MessagesFile: "compiler:Languages\Norwegian.isl"

[Files]
Source: "{#Payload}\bridge\*"; DestDir: "{app}\bridge"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#Payload}\tools\start-bridge-service.ps1"; DestDir: "{app}\tools"; Flags: ignoreversion

[Run]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File ""{app}\tools\start-bridge-service.ps1"" -Install"; WorkingDir: "{app}"; Flags: runhidden waituntilterminated; Description: "Installer og start Bridge-tjeneste"

[UninstallRun]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File ""{app}\tools\start-bridge-service.ps1"" -Uninstall"; WorkingDir: "{app}"; Flags: runhidden waituntilterminated

[Code]
#define Payload "build-new\payload"

function FindOmpBinary(): String;
var
  OmpPath: String;
  SearchPaths: array of String;
  I: Integer;
begin
  Result := '';
  // Search common paths
  SearchPaths := [
    ExpandConstant('%USERPROFILE%') + '\.bun\bin\omp.exe',
    ExpandConstant('%APPDATA%') + '\npm\omp.cmd',
    ExpandConstant('%LOCALAPPDATA%') + '\Programs\omp\omp.exe',
    'C:\Program Files\omp\omp.exe'
  ];
  for I := 0 to GetArrayLength(SearchPaths) - 1 do
  begin
    if FileExists(SearchPaths[I]) then
    begin
      Result := SearchPaths[I];
      break;
    end;
  end;
  // If found Bun shim, prefer Bun + cli.js
  if Result = ExpandConstant('%USERPROFILE%') + '\.bun\bin\omp.exe' then
  begin
    OmpPath := ExpandConstant('%USERPROFILE%') + '\.bun\install\global\node_modules\@oh-my-pi\pi-coding-agent\dist\cli.js';
    if FileExists(OmpPath) then
    begin
      // Return special format: bunExe|cliJs
      Result := 'C:\Users\' + ExpandConstant('{username}') + '\AppData\Local\Microsoft\WinGet\Packages\Oven-sh.Bun_Microsoft.Winget.Source_8wekyb3d8bbwe\bun-windows-x64\bun.exe' + '|' + OmpPath;
    end;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  BridgePath: String;
  ConfigPath: String;
  Token: String;
  OmpBin: String;
  UserName: String;
  ResultCode: Integer;
begin
  if CurStep = ssPostInstall then
  begin
    BridgePath := ExpandConstant('{app}\bridge');
    ConfigPath := BridgePath + '\config.json';

    // Create config.json if it doesn't exist
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

    // Find OMP binary
    OmpBin := FindOmpBinary();

    // Set OMP_BIN environment variable (user-level)
    if OmpBin <> '' then
    begin
      RegWriteStringValue(HKCU, 'Environment', 'OMP_BIN', OmpBin);
    end;

    // Write ompchat-commands.json to point to installed location
    SaveStringToFile(
      'C:\Users\' + ExpandConstant('{username}') + '\.omp\agent\ompchat-commands.json',
      '{' + #13#10 +
      '  "projectRoot": "' + ExpandConstant('{app}') + '",' + #13#10 +
      '  "bridge": "' + ExpandConstant('{app}') + '\\bridge\\omp-bridge.mjs"' + #13#10 +
      '}',
      False
    );
  end;
end;