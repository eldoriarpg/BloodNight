{ pkgs ? import <nixpkgs> {}, ... }:

let
jdk = pkgs.jdk21;
gradle= pkgs.gradle.override { java = jdk; };
in
pkgs.mkShell
{
  packages = with pkgs; [jdk gradle];
}

