package dz.usthb.eclipseworkspace.workspace.service.components;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;

public class MemberComponent implements WorkspaceComponent {

    private AppUser member;

    public MemberComponent(AppUser member) {
        this.member = member;
    }

    @Override
    public void display() {
        String fullName = member.getFirstName() + " " + member.getLastName();
        System.out.println("Member: " + fullName + " | Username: " + member.getUsername());
    }

    @Override
    public int getProgress() {
        // Members themselves don't carry progress — only tasks do.
        return 0;
    }

    // Optional helper if needed by your service or builder
    public AppUser getMember() {
        return member;
    }
}
