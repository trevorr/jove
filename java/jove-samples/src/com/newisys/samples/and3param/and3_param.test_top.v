module and3_param_test_top;
    parameter simulation_cycle = 100;

    reg SystemClock;
    wire clk;
    wire i1_1, i2_1;
    wire i1_2, i2_2;
    wire i1_3, i2_3;
    wire o1_1, i2_4;

    assign clk = SystemClock;

    And3Shell vshell();

    and3 dut0(
        .clk(clk),
        .i1(i1_1),
        .i2(i1_2),
        .i3(i1_3),
        .o1(o1_1));

    and3 dut1(
        .clk(clk),
        .i1(i2_1),
        .i2(i2_2),
        .i3(i2_3),
        .o1(o2_1));


    initial
    begin
        SystemClock = 0;
        forever
        begin
            #(simulation_cycle / 2)
            SystemClock = ~SystemClock;
        end
    end

endmodule

